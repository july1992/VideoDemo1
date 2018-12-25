//package com.vily.videodemo1.code;
//
//
//import android.test.AndroidTestCase;
//
///**
// *  * description : 
// *  * Author : Vily
// *  * Date : 2018/12/25
// *  
// **/
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaCodecList;
//import android.media.MediaFormat;
//import android.opengl.GLES20;
//import android.test.AndroidTestCase;
//import android.util.Log;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//import javax.microedition.khronos.opengles.GL10;
///**
// *生成一系列视频帧，对其进行编码，对其进行解码，并进行重要测试
// *与原始分歧。
// * <p>
// *我们将编码器输出缓冲区中的数据复制到运行的解码器输入缓冲区
// *他们并行。video / avc的第一个缓冲输出包含编解码器配置数据，
// *我们必须小心地转发到解码器。
// * <p>
// *另一种方法是将解码器的输出保存为mpeg4视频
// *文件，并从磁盘读回。我们生成的数据只是一个基本数据
// *流，所以我们需要执行其他步骤来实现这一目标。
// */
//public class EncodeDecodeTest extends AndroidTestCase {
//    private static final String TAG = "EncodeDecodeTest";
//    private static final boolean VERBOSE = false;           //大量的日志记录
//    private static final boolean DEBUG_SAVE_FILE = false;    //保存编码电影的副本
//    private static final String DEBUG_FILE_NAME_BASE = "/sdcard/test.";
//    //编码器的参数
//    private static final String MIME_TYPE = "video/avc";    // H.264高级视频编码
//    private static final int FRAME_RATE = 15;               // 15fps
//    private static final int IFRAME_INTERVAL = 10;          // I帧之间10秒
//    //电影长度，以帧为单位
//    private static final int NUM_FRAMES = 30;                //两秒钟的视频
//
//    private static final int TEST_Y = 120;                  //彩色矩形的YUV值
//    private static final int TEST_U = 160;
//    private static final int TEST_V = 200;
//    private static final int TEST_R0 = 0;                   // RGB等价于{0,0,0}
//    private static final int TEST_G0 = 136;
//    private static final int TEST_B0 = 0;
//    private static final int TEST_R1 = 236;                 // RGB等价物{120,160,200}
//    private static final int TEST_G1 = 50;
//    private static final int TEST_B1 = 186;
//    //帧的大小，以像素为单位
//    private int mWidth = -1;
//    private int mHeight = -1;
//    //比特率，以每秒位数为单位
//    private int mBitRate = -1;
//    //看到的最大颜色分量增量（即实际与预期）
//    private int mLargestColorDelta;
//    /**
//     *通过编码器和解码器测试AVC视频流。数据来自
//     *一系列byte []缓冲区并解码为ByteBuffers。检查输出
//     *有效性。
//     * /
//     */
//    public void testEncodeDecodeVideoFromBufferToBufferQCIF() throws Exception {
//        setParameters(176, 144, 1000000);
//        encodeDecodeVideoFromBuffer(false);
//    }
//    public void testEncodeDecodeVideoFromBufferToBufferQVGA() throws Exception {
//        setParameters(320, 240, 2000000);
//        encodeDecodeVideoFromBuffer(false);
//    }
//    public void testEncodeDecodeVideoFromBufferToBuffer720p() throws Exception {
//        setParameters(1280, 720, 6000000);
//        encodeDecodeVideoFromBuffer(false);
//    }
//    /**
//     *通过编码器和解码器测试AVC视频流。数据来自
//     *一系列byte []缓冲区并解码为Surfaces。检查输出
//     *有效性。
//     * <p>
//     *由于SurfaceTexture.OnFrameAvailableListener的工作方式，我们需要运行它
//     *测试没有配置Looper的线程。如果我们不这样做，那么测试就会
//     *传递，但我们实际上不会测试输出，因为我们永远不会收到“帧”
//     *可用“通知”。CTS测试框架似乎正在配置一个Looper
//     *测试线程，因此我们必须在一段时间内将控制权交给新线程
//     * 考试。
//     */
//    public void testEncodeDecodeVideoFromBufferToSurfaceQCIF() throws Throwable {
//        setParameters(176, 144, 1000000);
//        BufferToSurfaceWrapper.runTest(this);
//    }
//    public void testEncodeDecodeVideoFromBufferToSurfaceQVGA() throws Throwable {
//        setParameters(320, 240, 2000000);
//        BufferToSurfaceWrapper.runTest(this);
//    }
//    public void testEncodeDecodeVideoFromBufferToSurface720p() throws Throwable {
//        setParameters(1280, 720, 6000000);
//        BufferToSurfaceWrapper.runTest(this);
//    }
//    /** Wraps testEncodeDecodeVideoFromBuffer(true) */
//    private static class BufferToSurfaceWrapper implements Runnable {
//        private Throwable mThrowable;
//        private EncodeDecodeTest mTest;
//        private BufferToSurfaceWrapper(EncodeDecodeTest test) {
//            mTest = test;
//        }
//        @Override
//        public void run() {
//            try {
//                mTest.encodeDecodeVideoFromBuffer(true);
//            } catch (Throwable th) {
//                mThrowable = th;
//            }
//        }
//        /**
//         * Entry point.
//         */
//        public static void runTest(EncodeDecodeTest obj) throws Throwable {
//            BufferToSurfaceWrapper wrapper = new BufferToSurfaceWrapper(obj);
//            Thread th = new Thread(wrapper, "codec test");
//            th.start();
//            th.join();
//            if (wrapper.mThrowable != null) {
//                throw wrapper.mThrowable;
//            }
//        }
//    }
//    /**
//     *通过编码器和解码器测试AVC视频流。数据通过提供
//     * a Surface并解码到Surface上。检查输出的有效性。
//     */
//    public void testEncodeDecodeVideoFromSurfaceToSurfaceQCIF() throws Throwable {
//        setParameters(176, 144, 1000000);
//        SurfaceToSurfaceWrapper.runTest(this);
//    }
//    public void testEncodeDecodeVideoFromSurfaceToSurfaceQVGA() throws Throwable {
//        setParameters(320, 240, 2000000);
//        SurfaceToSurfaceWrapper.runTest(this);
//    }
//    public void testEncodeDecodeVideoFromSurfaceToSurface720p() throws Throwable {
//        setParameters(1280, 720, 6000000);
//        SurfaceToSurfaceWrapper.runTest(this);
//    }
//    /** Wraps testEncodeDecodeVideoFromSurfaceToSurface() */
//    private static class SurfaceToSurfaceWrapper implements Runnable {
//        private Throwable mThrowable;
//        private EncodeDecodeTest mTest;
//        private SurfaceToSurfaceWrapper(EncodeDecodeTest test) {
//            mTest = test;
//        }
//        @Override
//        public void run() {
//            try {
//                mTest.encodeDecodeVideoFromSurfaceToSurface();
//            } catch (Throwable th) {
//                mThrowable = th;
//            }
//        }
//        /**
//         * Entry point.
//         */
//        public static void runTest(EncodeDecodeTest obj) throws Throwable {
//            SurfaceToSurfaceWrapper wrapper = new SurfaceToSurfaceWrapper(obj);
//            Thread th = new Thread(wrapper, "codec test");
//            th.start();
//            th.join();
//            if (wrapper.mThrowable != null) {
//                throw wrapper.mThrowable;
//            }
//        }
//    }
//    /**
//     *  设置所需的帧大小和比特率。
//     */
//    private void setParameters(int width, int height, int bitRate) {
//        if ((width % 16) != 0 || (height % 16) != 0) {
//            Log.w(TAG, "WARNING: width or height not multiple of 16");
//        }
//        mWidth = width;
//        mHeight = height;
//        mBitRate = bitRate;
//    }
//    /**
//     *测试编码并随后解码生成到缓冲区中的帧的视频。
//     * <p>
//     *我们使用MediaCodec编码几帧视频测试模式，然后对其进行解码
//     *使用MediaCodec输出并进行一些简单的检查。
//     * <p>
//     *有关输入格式陷阱的讨论，请参见http://b.android.com/37769。
//     */
//    private void encodeDecodeVideoFromBuffer(boolean toSurface) throws Exception {
//        MediaCodec encoder = null;
//        MediaCodec decoder = null;
//        mLargestColorDelta = -1;
//        try {
//            MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
//            if (codecInfo == null) {
//                // Don't fail CTS if they don't have an AVC codec (not here, anyway).
//                Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
//                return;
//            }
//            if (VERBOSE) Log.d(TAG, "found codec: " + codecInfo.getName());
//            int colorFormat = selectColorFormat(codecInfo, MIME_TYPE);
//            if (VERBOSE) Log.d(TAG, "found colorFormat: " + colorFormat);
//            // We avoid the device-specific limitations on width and height by using values that
//            // are multiples of 16, which all tested devices seem to be able to handle.
//            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
//            // Set some properties.  Failing to specify some of these can cause the MediaCodec
//            // configure() call to throw an unhelpful exception.
//            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
//            format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
//            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
//            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
//            if (VERBOSE) Log.d(TAG, "format: " + format);
//            // Create a MediaCodec for the desired codec, then configure it as an encoder with
//            // our desired properties.
//            encoder = MediaCodec.createByCodecName(codecInfo.getName());
//            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            encoder.start();
//            // Create a MediaCodec for the decoder, just based on the MIME type.  The various
//            // format details will be passed through the csd-0 meta-data later on.
//            decoder = MediaCodec.createDecoderByType(MIME_TYPE);
//            doEncodeDecodeVideoFromBuffer(encoder, colorFormat, decoder, toSurface);
//        } finally {
//            if (VERBOSE) Log.d(TAG, "releasing codecs");
//            if (encoder != null) {
//                encoder.stop();
//                encoder.release();
//            }
//            if (decoder != null) {
//                decoder.stop();
//                decoder.release();
//            }
//            Log.i(TAG, "Largest color delta: " + mLargestColorDelta);
//        }
//    }
//    /**
//     *测试编码并随后解码生成到缓冲区中的帧的视频。
//     * <p>
//     *我们使用MediaCodec编码几帧视频测试模式，然后对其进行解码
//     *使用MediaCodec输出并进行一些简单的检查。
//     */
//    private void encodeDecodeVideoFromSurfaceToSurface() throws Exception {
//        MediaCodec encoder = null;
//        MediaCodec decoder = null;
//        InputSurface inputSurface = null;
//        OutputSurface outputSurface = null;
//        mLargestColorDelta = -1;
//        try {
//            MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
//            if (codecInfo == null) {
//                // Don't fail CTS if they don't have an AVC codec (not here, anyway).
//                Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
//                return;
//            }
//            if (VERBOSE) Log.d(TAG, "found codec: " + codecInfo.getName());
//            int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
//            //我们通过使用值来避免宽度和高度的设备特定限制
//            //是16的倍数，所有测试的设备似乎都能够处理。
//            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
//
//            //设置一些属性。未能指定其中一些可能导致MediaCodec
//            // configure（）调用抛出一个无用的异常。
//            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
//            format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
//            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
//            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
//            if (VERBOSE) Log.d(TAG, "format: " + format);
//            //创建输出曲面。
//            outputSurface = new OutputSurface(mWidth, mHeight);
//            //仅基于MIME类型为解码器创建MediaCodec。各种种类
//            //格式详细信息稍后将通过csd-0元数据传递。
//            decoder = MediaCodec.createDecoderByType(MIME_TYPE);
//            MediaFormat decoderFormat = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
//            decoder.configure(format, outputSurface.getSurface(), null, 0);
//            decoder.start();
//            //为所需的编解码器创建MediaCodec，然后将其配置为编码器
//            //我们想要的属性。请求Surface用于输入。
//            encoder = MediaCodec.createByCodecName(codecInfo.getName());
//            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            inputSurface = new InputSurface(encoder.createInputSurface());
//            encoder.start();
//            doEncodeDecodeVideoFromSurfaceToSurface(encoder, inputSurface, colorFormat, decoder, outputSurface);
//        } finally {
//            if (VERBOSE) Log.d(TAG, "releasing codecs");
//            if (inputSurface != null) {
//                inputSurface.release();
//            }
//            if (outputSurface != null) {
//                outputSurface.release();
//            }
//            if (encoder != null) {
//                encoder.stop();
//                encoder.release();
//            }
//            if (decoder != null) {
//                decoder.stop();
//                decoder.release();
//            }
//            Log.i(TAG, "Largest color delta: " + mLargestColorDelta);
//        }
//    }
//    /**
//     返回第一个能够编码指定MIME类型的编解码器，如果不是，则返回null
//     *匹配被发现。
//     */
//    private static MediaCodecInfo selectCodec(String mimeType) {
//        int numCodecs = MediaCodecList.getCodecCount();
//        for (int i = 0; i < numCodecs; i++) {
//            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
//            if (!codecInfo.isEncoder()) {
//                continue;
//            }
//            String[] types = codecInfo.getSupportedTypes();
//            for (int j = 0; j < types.length; j++) {
//                if (types[j].equalsIgnoreCase(mimeType)) {
//                    return codecInfo;
//                }
//            }
//        }
//        return null;
//    }
//    /**
//     *返回编解码器和此测试代码支持的颜色格式。如果不
//     *匹配被发现，这会导致测试失败 - 测试已知的一组格式
//     *应针对新平台进行扩展。
//     */
//    private static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
//        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
//        for (int i = 0; i < capabilities.colorFormats.length; i++) {
//            int colorFormat = capabilities.colorFormats[i];
//            if (isRecognizedFormat(colorFormat)) {
//                return colorFormat;
//            }
//        }
//        fail("couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
//        return 0;   // not reached
//    }
//    /**
//     *如果这是此测试代码理解的颜色格式（即我们知道如何），则返回true
//     *以此格式读取和生成帧）。
//     */
//    private static boolean isRecognizedFormat(int colorFormat) {
//        switch (colorFormat) {
//            // these are the formats we know how to handle for this test
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
//                return true;
//            default:
//                return false;
//        }
//    }
//    /**
//     *如果指定的颜色格式是半平面YUV，则返回true。抛出异常
//     *如果无法识别颜色格式（例如，不是YUV）。
//     */
//    private static boolean isSemiPlanarYUV(int colorFormat) {
//        switch (colorFormat) {
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
//                return false;
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
//                return true;
//            default:
//                throw new RuntimeException("unknown format " + colorFormat);
//        }
//    }
//    /**
//     *实际工作是从byte []的缓冲区编码帧。
//     */
//    private void doEncodeDecodeVideoFromBuffer(MediaCodec encoder, int encoderColorFormat,
//                                               MediaCodec decoder, boolean toSurface) {
//        final int TIMEOUT_USEC = 10000;
//        ByteBuffer[] encoderInputBuffers = encoder.getInputBuffers();
//        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
//        ByteBuffer[] decoderInputBuffers = null;
//        ByteBuffer[] decoderOutputBuffers = null;
//        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//        MediaFormat decoderOutputFormat = null;
//        int generateIndex = 0;
//        int checkIndex = 0;
//        int badFrames = 0;
//        boolean decoderConfigured = false;
//        OutputSurface outputSurface = null;
//        //我们处理的格式的视频数据帧的大小是stride * sliceHeight
//        //对于Y，以及（stride / 2）*（sliceHeight / 2）对于每个Cb和Cr通道。应用
//        //代数并假设stride == width和sliceHeight == height yield：
//        byte[] frameData = new byte[mWidth * mHeight * 3 / 2];
//        // Just out of curiosity.
//        long rawSize = 0;
//        long encodedSize = 0;
//        //将副本保存到磁盘。用于调试测试。请注意，这是一个原始的基础
//        //流，而不是.mp4文件，因此并非所有玩家都知道如何处理它。
//        FileOutputStream outputStream = null;
//        if (DEBUG_SAVE_FILE) {
//            String fileName = DEBUG_FILE_NAME_BASE + mWidth + "x" + mHeight + ".mp4";
//            try {
//                outputStream = new FileOutputStream(fileName);
//                Log.d(TAG, "encoded output will be saved as " + fileName);
//            } catch (IOException ioe) {
//                Log.w(TAG, "Unable to create debug output file " + fileName);
//                throw new RuntimeException(ioe);
//            }
//        }
//        if (toSurface) {
//            outputSurface = new OutputSurface(mWidth, mHeight);
//        }
//        // Loop until the output side is done.
//        boolean inputDone = false;
//        boolean encoderDone = false;
//        boolean outputDone = false;
//        while (!outputDone) {
//            if (VERBOSE) Log.d(TAG, "loop");
//
//            //如果我们没有提交框架，请生成一个新框架并提交。通过
//            //在我们正在努力确保编码器始终具有的每个循环上执行此操作
//            // 要做的工作。
//            //
//            //我们真的不想在这里超时，但有时会有延迟开放
//            //编码器设备，所以短暂的超时可以阻止我们努力。
//            if (!inputDone) {
//                int inputBufIndex = encoder.dequeueInputBuffer(TIMEOUT_USEC);
//                if (VERBOSE) Log.d(TAG, "inputBufIndex=" + inputBufIndex);
//                if (inputBufIndex >= 0) {
//                    long ptsUsec = computePresentationTime(generateIndex);
//                    if (generateIndex == NUM_FRAMES) {
//                        //发送一个设置了end-of-stream标志的空帧。如果我们设置EOS
//                        //在有数据的帧上，该帧数据将被忽略，并且
//                        //输出将是一帧短。
//                        encoder.queueInputBuffer(inputBufIndex, 0, 0, ptsUsec,
//                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                        inputDone = true;
//                        if (VERBOSE) Log.d(TAG, "sent input EOS (with zero-length frame)");
//                    } else {
//                        generateFrame(generateIndex, encoderColorFormat, frameData);
//                        ByteBuffer inputBuf = encoderInputBuffers[inputBufIndex];
//                        // the buffer should be sized to hold one full frame
//                        assertTrue(inputBuf.capacity() >= frameData.length);
//                        inputBuf.clear();
//                        inputBuf.put(frameData);
//                        encoder.queueInputBuffer(inputBufIndex, 0, frameData.length, ptsUsec, 0);
//                        if (VERBOSE) Log.d(TAG, "submitted frame " + generateIndex + " to enc");
//                    }
//                    generateIndex++;
//                } else {
//                    // either all in use, or we timed out during initial setup
//                    if (VERBOSE) Log.d(TAG, "input buffer not available");
//                }
//            }
//            //检查编码器的输出。如果还没有输出，我们要么需要
//            //提供更多输入，或者我们需要等待编码器发挥其魔力。我们
//            //实际上无法判断出是哪种情况，所以如果我们无法正确获取输出缓冲区
//            //我们走开，看看是否需要更多输入。
//            //
//            //一旦我们从编码器获得EOS，我们就不再需要这样做了。
//            if (!encoderDone) {
//                int encoderStatus = encoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
//                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                    // no output available yet
//                    if (VERBOSE) Log.d(TAG, "no output from encoder available");
//                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                    // not expected for an encoder
//                    encoderOutputBuffers = encoder.getOutputBuffers();
//                    if (VERBOSE) Log.d(TAG, "encoder output buffers changed");
//                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    // not expected for an encoder
//                    MediaFormat newFormat = encoder.getOutputFormat();
//                    if (VERBOSE) Log.d(TAG, "encoder output format changed: " + newFormat);
//                } else if (encoderStatus < 0) {
//                    fail("unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
//                } else { // encoderStatus >= 0
//                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
//                    if (encodedData == null) {
//                        fail("encoderOutputBuffer " + encoderStatus + " was null");
//                    }
//                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
//                    encodedData.position(info.offset);
//                    encodedData.limit(info.offset + info.size);
//                    encodedSize += info.size;
//                    if (outputStream != null) {
//                        byte[] data = new byte[info.size];
//                        encodedData.get(data);
//                        encodedData.position(info.offset);
//                        try {
//                            outputStream.write(data);
//                        } catch (IOException ioe) {
//                            Log.w(TAG, "failed writing debug data to file");
//                            throw new RuntimeException(ioe);
//                        }
//                    }
//                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                        //编解码器配置信息。仅在第一个数据包上预期。一种方法
//                        //处理这是手动将数据填充到MediaFormat中
//                        //并将其传递给configure（）。我们在这里练习API。
//                        assertFalse(decoderConfigured);
//                        MediaFormat format =
//                                MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
//                        format.setByteBuffer("csd-0", encodedData);
//                        decoder.configure(format, toSurface ? outputSurface.getSurface() : null,
//                                null, 0);
//                        decoder.start();
//                        decoderInputBuffers = decoder.getInputBuffers();
//                        decoderOutputBuffers = decoder.getOutputBuffers();
//                        decoderConfigured = true;
//                        if (VERBOSE) Log.d(TAG, "decoder configured (" + info.size + " bytes)");
//                    } else {
//                        // Get a decoder input buffer, blocking until it's available.
//                        assertTrue(decoderConfigured);
//                        int inputBufIndex = decoder.dequeueInputBuffer(-1);
//                        ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
//                        inputBuf.clear();
//                        inputBuf.put(encodedData);
//                        decoder.queueInputBuffer(inputBufIndex, 0, info.size,
//                                info.presentationTimeUs, info.flags);
//                        encoderDone = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
//                        if (VERBOSE) Log.d(TAG, "passed " + info.size + " bytes to decoder"
//                                + (encoderDone ? " (EOS)" : ""));
//                    }
//                    encoder.releaseOutputBuffer(encoderStatus, false);
//                }
//            }
//            //检查解码器的输出。我们希望在每个循环中执行此操作以避免
//            //拖延管道的可能性 我们使用短暂的超时来避免
//            //如果解码器很难工作但是下一帧还没准备好，那就烧掉CPU。
//            //
//            //如果我们要解码到Surface，我们会像往常一样通知此处，但是
//            // ByteBuffer引用将为null。数据将发送到Surface。
//            if (decoderConfigured) {
//                int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
//                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                    //还没有可用的输出
//                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
//                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                    //与直接ByteBuffer关联的存储可能已经取消映射，
//                    //所以试图通过旧的输出缓冲区数组访问数据可以
//                    //导致本机崩溃
//                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
//                    decoderOutputBuffers = decoder.getOutputBuffers();
//                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    //这发生在返回第一帧之前
//                    decoderOutputFormat = decoder.getOutputFormat();
//                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " +
//                            decoderOutputFormat);
//                } else if (decoderStatus < 0) {
//                    fail("unexpected result from deocder.dequeueOutputBuffer: " + decoderStatus);
//                } else {  // decoderStatus >= 0
//                    if (!toSurface) {
//                        ByteBuffer outputFrame = decoderOutputBuffers[decoderStatus];
//                        outputFrame.position(info.offset);
//                        outputFrame.limit(info.offset + info.size);
//                        rawSize += info.size;
//                        if (info.size == 0) {
//                            if (VERBOSE) Log.d(TAG, "got empty frame");
//                        } else {
//                            if (VERBOSE) Log.d(TAG, "decoded, checking frame " + checkIndex);
//                            assertEquals("Wrong time stamp", computePresentationTime(checkIndex),
//                                    info.presentationTimeUs);
//                            if (!checkFrame(checkIndex++, decoderOutputFormat, outputFrame)) {
//                                badFrames++;
//                            }
//                        }
//                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                            if (VERBOSE) Log.d(TAG, "output EOS");
//                            outputDone = true;
//                        }
//                        decoder.releaseOutputBuffer(decoderStatus, false /*render*/);
//                    } else {
//                        if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
//                                " (size=" + info.size + ")");
//                        rawSize += info.size;
//                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                            if (VERBOSE) Log.d(TAG, "output EOS");
//                            outputDone = true;
//                        }
//                        boolean doRender = (info.size != 0);
//                        //一旦我们调用releaseOutputBuffer，就会转发缓冲区
//                        //将SurfaceTexture转换为纹理。API不保证
//                        //在调用返回之前纹理将可用，所以我们
//                        //需要等待onFrameAvailable回调才能触发。
//                        decoder.releaseOutputBuffer(decoderStatus, doRender);
//                        if (doRender) {
//                            if (VERBOSE) Log.d(TAG, "awaiting frame " + checkIndex);
//                            assertEquals("Wrong time stamp", computePresentationTime(checkIndex),
//                                    info.presentationTimeUs);
//                            outputSurface.awaitNewImage();
//                            outputSurface.drawImage();
//                            if (!checkSurfaceFrame(checkIndex++)) {
//                                badFrames++;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if (VERBOSE) Log.d(TAG, "decoded " + checkIndex + " frames at "
//                + mWidth + "x" + mHeight + ": raw=" + rawSize + ", enc=" + encodedSize);
//        if (outputStream != null) {
//            try {
//                outputStream.close();
//            } catch (IOException ioe) {
//                Log.w(TAG, "failed closing debug file");
//                throw new RuntimeException(ioe);
//            }
//        }
//        if (outputSurface != null) {
//            outputSurface.release();
//        }
//        if (checkIndex != NUM_FRAMES) {
//            fail("expected " + NUM_FRAMES + " frames, only decoded " + checkIndex);
//        }
//        if (badFrames != 0) {
//            fail("Found " + badFrames + " bad frames");
//        }
//    }
//    /**
//     *从Surface到Surface的编码和解码实际工作。
//     */
//    private void doEncodeDecodeVideoFromSurfaceToSurface(MediaCodec encoder,
//                                                         InputSurface inputSurface, int encoderColorFormat, MediaCodec decoder,
//                                                         OutputSurface outputSurface) {
//        final int TIMEOUT_USEC = 10000;
//        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
//        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
//        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//        int generateIndex = 0;
//        int checkIndex = 0;
//        int badFrames = 0;
//        //将副本保存到磁盘。用于调试测试。请注意，这是一个原始的基础
//        //流，而不是.mp4文件，因此并非所有玩家都知道如何处理它。
//        FileOutputStream outputStream = null;
//        if (DEBUG_SAVE_FILE) {
//            String fileName = DEBUG_FILE_NAME_BASE + mWidth + "x" + mHeight + ".mp4";
//            try {
//                outputStream = new FileOutputStream(fileName);
//                Log.d(TAG, "encoded output will be saved as " + fileName);
//            } catch (IOException ioe) {
//                Log.w(TAG, "Unable to create debug output file " + fileName);
//                throw new RuntimeException(ioe);
//            }
//        }
//        //循环直到输出端完成。
//        boolean inputDone = false;
//        boolean encoderDone = false;
//        boolean outputDone = false;
//        while (!outputDone) {
//            if (VERBOSE) Log.d(TAG, "loop");
//            //如果我们没有提交框架，请生成一个新框架并提交。该
//            //如果输入已满，eglSwapBuffers调用将阻塞。
//            if (!inputDone) {
//                if (generateIndex == NUM_FRAMES) {
//                    // Send an empty frame with the end-of-stream flag set.
//                    if (VERBOSE) Log.d(TAG, "signaling input EOS");
//                    encoder.signalEndOfInputStream();
//                    inputDone = true;
//                } else {
//                    inputSurface.makeCurrent();
//                    generateSurfaceFrame(generateIndex);
//                    inputSurface.setPresentationTime(computePresentationTime(generateIndex) * 1000);
//                    if (VERBOSE) Log.d(TAG, "inputSurface swapBuffers");
//                    inputSurface.swapBuffers();
//                }
//                generateIndex++;
//            }
//            //假设输出可用。循环，直到两个假设都是假的。
//            boolean decoderOutputAvailable = true;
//            boolean encoderOutputAvailable = !encoderDone;
//            while (decoderOutputAvailable || encoderOutputAvailable) {
//                //首先从解码器中排出任何待处理的输出。这很重要
//                //在我们尝试填充更多数据之前执行此操作。
//                int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
//                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                    //还没有可用的输出
//                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
//                    decoderOutputAvailable = false;
//                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed (but we don't care)");
//                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    // this happens before the first frame is returned
//                    MediaFormat decoderOutputFormat = decoder.getOutputFormat();
//                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " +
//                            decoderOutputFormat);
//                } else if (decoderStatus < 0) {
//                    fail("unexpected result from deocder.dequeueOutputBuffer: " + decoderStatus);
//                } else {  // decoderStatus >= 0
//                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
//                            " (size=" + info.size + ")");
//                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                        if (VERBOSE) Log.d(TAG, "output EOS");
//                        outputDone = true;
//                    }
//                    // ByteBuffers是空引用，但我们仍然得到非零大小
//                    //解码数据
//                    boolean doRender = (info.size != 0);
//                    //一旦我们调用releaseOutputBuffer，就会转发缓冲区
//                    //将SurfaceTexture转换为纹理。API不保证
//                    //在调用返回之前纹理将可用，所以我们
//                    //需要等待onFrameAvailable回调才能触发。如果我们不这样做
//                    //等等，我们冒着丢帧的风险。
//                    outputSurface.makeCurrent();
//                    decoder.releaseOutputBuffer(decoderStatus, doRender);
//                    if (doRender) {
//                        assertEquals("Wrong time stamp", computePresentationTime(checkIndex),
//                                info.presentationTimeUs);
//                        if (VERBOSE) Log.d(TAG, "awaiting frame " + checkIndex);
//                        outputSurface.awaitNewImage();
//                        outputSurface.drawImage();
//                        if (!checkSurfaceFrame(checkIndex++)) {
//                            badFrames++;
//                        }
//                    }
//                }
//                if (decoderStatus != MediaCodec.INFO_TRY_AGAIN_LATER) {
//                    // Continue attempts to drain output.
//                    continue;
//                }
//                //解码器被耗尽，检查我们是否有一个新的输出缓冲区
//                //编码器
//                if (!encoderDone) {
//                    int encoderStatus = encoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
//                    if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                        // no output available yet
//                        if (VERBOSE) Log.d(TAG, "no output from encoder available");
//                        encoderOutputAvailable = false;
//                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                        // not expected for an encoder
//                        encoderOutputBuffers = encoder.getOutputBuffers();
//                        if (VERBOSE) Log.d(TAG, "encoder output buffers changed");
//                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                        // not expected for an encoder
//                        MediaFormat newFormat = encoder.getOutputFormat();
//                        if (VERBOSE) Log.d(TAG, "encoder output format changed: " + newFormat);
//                    } else if (encoderStatus < 0) {
//                        fail("unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
//                    } else { // encoderStatus >= 0
//                        ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
//                        if (encodedData == null) {
//                            fail("encoderOutputBuffer " + encoderStatus + " was null");
//                        }
//                        // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
//                        encodedData.position(info.offset);
//                        encodedData.limit(info.offset + info.size);
//                        if (outputStream != null) {
//                            byte[] data = new byte[info.size];
//                            encodedData.get(data);
//                            encodedData.position(info.offset);
//                            try {
//                                outputStream.write(data);
//                            } catch (IOException ioe) {
//                                Log.w(TAG, "failed writing debug data to file");
//                                throw new RuntimeException(ioe);
//                            }
//                        }
//                        //获取解码器输入缓冲区，阻塞直到可用。我们刚刚
//                        //排除了解码器输出，所以我们期望有一个免费输入
//                        //缓冲现在或在不久的将来（即这应该永远不会死锁
//                        //如果编解码器满足要求）。
//                        //
//                        //我们获得的第一个数据缓冲区将具有BUFFER_FLAG_CODEC_CONFIG
//                        // flag set; 解码器将看到此并完成自身配置。
//                        int inputBufIndex = decoder.dequeueInputBuffer(-1);
//                        ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
//                        inputBuf.clear();
//                        inputBuf.put(encodedData);
//                        decoder.queueInputBuffer(inputBufIndex, 0, info.size,
//                                info.presentationTimeUs, info.flags);
//                        //如果来自编码器的所有内容都已传递给解码器，我们
//                        //可以停止轮询编码器输出。（这只是一个优化。）
//                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                            encoderDone = true;
//                            encoderOutputAvailable = false;
//                        }
//                        if (VERBOSE) Log.d(TAG, "passed " + info.size + " bytes to decoder"
//                                + (encoderDone ? " (EOS)" : ""));
//                        encoder.releaseOutputBuffer(encoderStatus, false);
//                    }
//                }
//            }
//        }
//        if (outputStream != null) {
//            try {
//                outputStream.close();
//            } catch (IOException ioe) {
//                Log.w(TAG, "failed closing debug file");
//                throw new RuntimeException(ioe);
//            }
//        }
//        if (checkIndex != NUM_FRAMES) {
//            fail("expected " + NUM_FRAMES + " frames, only decoded " + checkIndex);
//        }
//        if (badFrames != 0) {
//            fail("Found " + badFrames + " bad frames");
//        }
//    }
//    /**
//     / **
//     *将帧N的数据生成到提供的缓冲区中。我们有一个8帧动画
//     *包裹的序列。它看起来像这样：
//     * <pre>
//     * 0 1 2 3
//     * 7 6 5 4
//     * </ pre>
//     *我们绘制八个矩形中的一个，并将其余部分设置为零填充颜色。
//     * /
//     */
//    private void generateFrame(int frameIndex, int colorFormat, byte[] frameData) {
//        final int HALF_WIDTH = mWidth / 2;
//        boolean semiPlanar = isSemiPlanarYUV(colorFormat);
//        // Set to zero.  In YUV this is a dull green.
//        Arrays.fill(frameData, (byte) 0);
//        int startX, startY, countX, countY;
//        frameIndex %= 8;
//        //frameIndex = (frameIndex / 8) % 8;    // use this instead for debug -- easier to see
//        if (frameIndex < 4) {
//            startX = frameIndex * (mWidth / 4);
//            startY = 0;
//        } else {
//            startX = (7 - frameIndex) * (mWidth / 4);
//            startY = mHeight / 2;
//        }
//        for (int y = startY + (mHeight/2) - 1; y >= startY; --y) {
//            for (int x = startX + (mWidth/4) - 1; x >= startX; --x) {
//                if (semiPlanar) {
//                    //全尺寸Y，然后是半分辨率的UV对
//                    //例如Nexus 4 OMX.qcom.video.encoder.avc COLOR_FormatYUV420SemiPlanar
//                    //例如Galaxy Nexus OMX.TI.DUCATI1.VIDEO.H264E
//                    // OMX_TI_COLOR_FormatYUV420PackedSemiPlanar
//                    frameData[y * mWidth + x] = (byte) TEST_Y;
//                    if ((x & 0x01) == 0 && (y & 0x01) == 0) {
//                        frameData[mWidth*mHeight + y * HALF_WIDTH + x] = (byte) TEST_U;
//                        frameData[mWidth*mHeight + y * HALF_WIDTH + x + 1] = (byte) TEST_V;
//                    }
//                } else {
//                    //全尺寸Y，其次是四分之一尺寸U和四分之一尺寸V.
//                    //例如Nexus 10 OMX.Exynos.AVC.Encoder COLOR_FormatYUV420Planar
//                    //例如Nexus 7 OMX.Nvidia.h264.encoder COLOR_FormatYUV420Planar
//                    frameData[y * mWidth + x] = (byte) TEST_Y;
//                    if ((x & 0x01) == 0 && (y & 0x01) == 0) {
//                        frameData[mWidth*mHeight + (y/2) * HALF_WIDTH + (x/2)] = (byte) TEST_U;
//                        frameData[mWidth*mHeight + HALF_WIDTH * (mHeight / 2) +
//                                (y/2) * HALF_WIDTH + (x/2)] = (byte) TEST_V;
//                    }
//                }
//            }
//        }
//    }
//    /**
//     *执行简单检查以确定框架是否正确。
//     * <p>
//     *有关布局的说明，请参阅{@link #generateFrame}。想法是抽样
//     *来自8个区域中间的一个像素，并验证正确的区域
//     *非背景色。我们无法准确知道视频编码器的功能
//     *我们的框架，所以我们只是检查它是否正常或多或少。
//     *
//     * @return如果框架看起来很好，则为true
//     */
//    private boolean checkFrame(int frameIndex, MediaFormat format, ByteBuffer frameData) {
//        //检查我们不理解的颜色格式。没有视频要求
//        //解码器使用“世俗”格式，所以我们只是传递专有格式。
//        //例如Nexus 4 0x7FA30C03 OMX_QCOM_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka
//        int colorFormat = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
//        if (!isRecognizedFormat(colorFormat)) {
//            Log.d(TAG, "unable to check frame contents for colorFormat=" +
//                    Integer.toHexString(colorFormat));
//            return true;
//        }
//        boolean frameFailed = false;
//        boolean semiPlanar = isSemiPlanarYUV(colorFormat);
//        int width = format.getInteger(MediaFormat.KEY_WIDTH);
//        int height = format.getInteger(MediaFormat.KEY_HEIGHT);
//        int halfWidth = width / 2;
//        int cropLeft = format.getInteger("crop-left");
//        int cropRight = format.getInteger("crop-right");
//        int cropTop = format.getInteger("crop-top");
//        int cropBottom = format.getInteger("crop-bottom");
//        int cropWidth = cropRight - cropLeft + 1;
//        int cropHeight = cropBottom - cropTop + 1;
//        assertEquals(mWidth, cropWidth);
//        assertEquals(mHeight, cropHeight);
//        for (int i = 0; i < 8; i++) {
//            int x, y;
//            if (i < 4) {
//                x = i * (mWidth / 4) + (mWidth / 8);
//                y = mHeight / 4;
//            } else {
//                x = (7 - i) * (mWidth / 4) + (mWidth / 8);
//                y = (mHeight * 3) / 4;
//            }
//            y += cropTop;
//            x += cropLeft;
//            int testY, testU, testV;
//            if (semiPlanar) {
//                // Galaxy Nexus uses OMX_TI_COLOR_FormatYUV420PackedSemiPlanar
//                testY = frameData.get(y * width + x) & 0xff;
//                testU = frameData.get(width*height + 2*(y/2) * halfWidth + 2*(x/2)) & 0xff;
//                testV = frameData.get(width*height + 2*(y/2) * halfWidth + 2*(x/2) + 1) & 0xff;
//            } else {
//                // Nexus 10, Nexus 7 use COLOR_FormatYUV420Planar
//                testY = frameData.get(y * width + x) & 0xff;
//                testU = frameData.get(width*height + (y/2) * halfWidth + (x/2)) & 0xff;
//                testV = frameData.get(width*height + halfWidth * (height / 2) +
//                        (y/2) * halfWidth + (x/2)) & 0xff;
//            }
//            int expY, expU, expV;
//            if (i == frameIndex % 8) {
//                // colored rect
//                expY = TEST_Y;
//                expU = TEST_U;
//                expV = TEST_V;
//            } else {
//                // should be our zeroed-out buffer
//                expY = expU = expV = 0;
//            }
//            if (!isColorClose(testY, expY) ||
//                    !isColorClose(testU, expU) ||
//                    !isColorClose(testV, expV)) {
//                Log.w(TAG, "Bad frame " + frameIndex + " (rect=" + i + ": yuv=" + testY +
//                        "," + testU + "," + testV + " vs. expected " + expY + "," + expU +
//                        "," + expV + ")");
//                frameFailed = true;
//            }
//        }
//        return !frameFailed;
//    }
//    /**
//     *使用GL命令生成一帧数据。
//     */
//    private void generateSurfaceFrame(int frameIndex) {
//        frameIndex %= 8;
//        int startX, startY;
//        if (frameIndex < 4) {
//            // (0,0) is bottom-left in GL
//            startX = frameIndex * (mWidth / 4);
//            startY = mHeight / 2;
//        } else {
//            startX = (7 - frameIndex) * (mWidth / 4);
//            startY = 0;
//        }
//        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
//        GLES20.glClearColor(TEST_R0 / 255.0f, TEST_G0 / 255.0f, TEST_B0 / 255.0f, 1.0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
//        GLES20.glScissor(startX, startY, mWidth / 4, mHeight / 2);
//        GLES20.glClearColor(TEST_R1 / 255.0f, TEST_G1 / 255.0f, TEST_B1 / 255.0f, 1.0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//    }
//    /**
//     *检查框架的正确性。与{@link #checkFrame}类似，但使用GL
//     *从当前曲面读取像素。
//     *
//     * @return如果框架看起来很好，则为true
//     */
//    private boolean checkSurfaceFrame(int frameIndex) {
//        ByteBuffer pixelBuf = ByteBuffer.allocateDirect(4); // TODO - reuse this
//        boolean frameFailed = false;
//        for (int i = 0; i < 8; i++) {
//            // Note the coordinates are inverted on the Y-axis in GL.
//            int x, y;
//            if (i < 4) {
//                x = i * (mWidth / 4) + (mWidth / 8);
//                y = (mHeight * 3) / 4;
//            } else {
//                x = (7 - i) * (mWidth / 4) + (mWidth / 8);
//                y = mHeight / 4;
//            }
//            GLES20.glReadPixels(x, y, 1, 1, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixelBuf);
//            int r = pixelBuf.get(0) & 0xff;
//            int g = pixelBuf.get(1) & 0xff;
//            int b = pixelBuf.get(2) & 0xff;
//            //Log.d(TAG, "GOT(" + frameIndex + "/" + i + "): r=" + r + " g=" + g + " b=" + b);
//            int expR, expG, expB;
//            if (i == frameIndex % 8) {
//                // colored rect
//                expR = TEST_R1;
//                expG = TEST_G1;
//                expB = TEST_B1;
//            } else {
//                // zero background color
//                expR = TEST_R0;
//                expG = TEST_G0;
//                expB = TEST_B0;
//            }
//            if (!isColorClose(r, expR) ||
//                    !isColorClose(g, expG) ||
//                    !isColorClose(b, expB)) {
//                Log.w(TAG, "Bad frame " + frameIndex + " (rect=" + i + ": rgb=" + r +
//                        "," + g + "," + b + " vs. expected " + expR + "," + expG +
//                        "," + expB + ")");
//                frameFailed = true;
//            }
//        }
//        return !frameFailed;
//    }
//    /**
//     *如果实际颜色值接近预期颜色值，则返回true。更新
//     * mLargestColorDelta。
//     */
//    boolean isColorClose(int actual, int expected) {
//        final int MAX_DELTA = 8;
//        int delta = Math.abs(actual - expected);
//        if (delta > mLargestColorDelta) {
//            mLargestColorDelta = delta;
//        }
//        return (delta <= MAX_DELTA);
//    }
//    /**
//     *生成帧N的呈现时间，以微秒为单位。
//     */
//    private static long computePresentationTime(int frameIndex) {
//        return 132 + frameIndex * 1000000 / FRAME_RATE;
//    }
//}