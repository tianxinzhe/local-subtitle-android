#include <jni.h>
#include <string>
#include <cstring>
#include <android/log.h>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avutil.h>
#include <libavutil/samplefmt.h>
#include <libswresample/swresample.h>
}

#define LOG_TAG "FFmpegBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static constexpr int TARGET_SAMPLE_RATE = 16000;
static constexpr int TARGET_CHANNELS = 1;
static constexpr AVSampleFormat TARGET_FORMAT = AV_SAMPLE_FMT_S16;

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_lemonsubtitle_FFmpegBridge_nativeExtractAudio(
    JNIEnv* env,
    jobject /* this */,
    jstring input_path,
    jstring output_path
) {
    const char* input = env->GetStringUTFChars(input_path, nullptr);
    const char* output = env->GetStringUTFChars(output_path, nullptr);

    LOGI("Extracting audio: %s -> %s", input, output);

    avformat_network_init();
    AVFormatContext* fmt_ctx = nullptr;
    int ret = avformat_open_input(&fmt_ctx, input, nullptr, nullptr);
    if (ret < 0) {
        char err[256];
        av_strerror(ret, err, sizeof(err));
        LOGE("avformat_open_input failed: %s", err);
        env->ReleaseStringUTFChars(input_path, input);
        env->ReleaseStringUTFChars(output_path, output);
        return env->NewStringUTF(err);
    }

    ret = avformat_find_stream_info(fmt_ctx, nullptr);
    if (ret < 0) {
        LOGE("avformat_find_stream_info failed");
        avformat_close_input(&fmt_ctx);
        env->ReleaseStringUTFChars(input_path, input);
        env->ReleaseStringUTFChars(output_path, output);
        return env->NewStringUTF("avformat_find_stream_info failed");
    }

    int audio_stream_idx = -1;
    const AVCodec* codec = nullptr;
    for (unsigned i = 0; i < fmt_ctx->nb_streams; i++) {
        if (fmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_stream_idx = i;
            codec = avcodec_find_decoder(fmt_ctx->streams[i]->codecpar->codec_id);
            break;
        }
    }

    if (audio_stream_idx < 0 || !codec) {
        LOGE("No audio stream found");
        avformat_close_input(&fmt_ctx);
        env->ReleaseStringUTFChars(input_path, input);
        env->ReleaseStringUTFChars(output_path, output);
        return env->NewStringUTF("No audio stream found");
    }

    AVCodecContext* codec_ctx = avcodec_alloc_context3(codec);
    avcodec_parameters_to_context(codec_ctx, fmt_ctx->streams[audio_stream_idx]->codecpar);
    ret = avcodec_open2(codec_ctx, codec, nullptr);
    if (ret < 0) {
        LOGE("avcodec_open2 failed");
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        env->ReleaseStringUTFChars(input_path, input);
        env->ReleaseStringUTFChars(output_path, output);
        return env->NewStringUTF("avcodec_open2 failed");
    }

    SwrContext* swr = swr_alloc_set_opts(
        nullptr,
        av_get_default_channel_layout(TARGET_CHANNELS),
        TARGET_FORMAT,
        TARGET_SAMPLE_RATE,
        av_get_default_channel_layout(codec_ctx->ch_layout.nb_channels),
        codec_ctx->sample_fmt,
        codec_ctx->sample_rate,
        0, nullptr
    );
    if (!swr || swr_init(swr) < 0) {
        LOGE("swr_init failed");
        swr_free(&swr);
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        env->ReleaseStringUTFChars(input_path, input);
        env->ReleaseStringUTFChars(output_path, output);
        return env->NewStringUTF("swr_init failed");
    }

    FILE* out_file = fopen(output, "wb");
    if (!out_file) {
        LOGE("Cannot open output file");
        swr_free(&swr);
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        env->ReleaseStringUTFChars(input_path, input);
        env->ReleaseStringUTFChars(output_path, output);
        return env->NewStringUTF("Cannot open output file");
    }

    // Write WAV header (placeholder, will rewrite at end)
    uint32_t data_size = 0;
    fwrite("RIFF----WAVEfmt ", 1, 16, out_file);
    uint32_t fmt_chunk_size = 16;
    uint16_t audio_format = 1; // PCM
    uint16_t num_channels = TARGET_CHANNELS;
    uint32_t sample_rate = TARGET_SAMPLE_RATE;
    uint16_t bits_per_sample = 16;
    uint16_t block_align = num_channels * bits_per_sample / 8;
    uint32_t byte_rate = sample_rate * block_align;
    fwrite(&fmt_chunk_size, 4, 1, out_file);
    fwrite(&audio_format, 2, 1, out_file);
    fwrite(&num_channels, 2, 1, out_file);
    fwrite(&sample_rate, 4, 1, out_file);
    fwrite(&byte_rate, 4, 1, out_file);
    fwrite(&block_align, 2, 1, out_file);
    fwrite(&bits_per_sample, 2, 1, out_file);
    fwrite("data----", 1, 8, out_file);

    AVPacket* packet = av_packet_alloc();
    AVFrame* frame = av_frame_alloc();
    uint8_t* resampled_buf = (uint8_t*)av_malloc(TARGET_SAMPLE_RATE * TARGET_CHANNELS * 2);

    while (av_read_frame(fmt_ctx, packet) >= 0) {
        if (packet->stream_index == audio_stream_idx) {
            ret = avcodec_send_packet(codec_ctx, packet);
            if (ret < 0) continue;

            while (ret >= 0) {
                ret = avcodec_receive_frame(codec_ctx, frame);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) break;

                if (ret >= 0) {
                    uint8_t* output_buf[1] = { resampled_buf };
                    int samples = swr_convert(
                        swr, output_buf, frame->nb_samples,
                        (const uint8_t**)frame->data, frame->nb_samples
                    );
                    if (samples > 0) {
                        int bytes = samples * TARGET_CHANNELS * 2;
                        fwrite(resampled_buf, 1, bytes, out_file);
                        data_size += bytes;
                    }
                }
            }
        }
        av_packet_unref(packet);
    }

    // Flush decoder
    ret = avcodec_send_packet(codec_ctx, nullptr);
    while (ret >= 0) {
        ret = avcodec_receive_frame(codec_ctx, frame);
        if (ret == AVERROR_EOF) break;
        if (ret >= 0) {
            uint8_t* output_buf[1] = { resampled_buf };
            int samples = swr_convert(
                swr, output_buf, frame->nb_samples,
                (const uint8_t**)frame->data, frame->nb_samples
            );
            if (samples > 0) {
                int bytes = samples * TARGET_CHANNELS * 2;
                fwrite(resampled_buf, 1, bytes, out_file);
                data_size += bytes;
            }
        }
    }

    // Update WAV header
    fseek(out_file, 4, SEEK_SET);
    uint32_t file_size = 36 + data_size;
    fwrite(&file_size, 4, 1, out_file);
    fseek(out_file, 40, SEEK_SET);
    fwrite(&data_size, 4, 1, out_file);

    fclose(out_file);
    av_free(resampled_buf);
    av_frame_free(&frame);
    av_packet_free(&packet);
    swr_free(&swr);
    avcodec_free_context(&codec_ctx);
    avformat_close_input(&fmt_ctx);

    LOGI("Audio extraction complete, %u bytes written", data_size);

    env->ReleaseStringUTFChars(input_path, input);
    env->ReleaseStringUTFChars(output_path, output);
    return env->NewStringUTF("");
}

} // extern "C"
