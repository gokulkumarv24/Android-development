#include <jni.h>
#include <opencv2/opencv.hpp>
#include <vector>

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_edgedetector_CameraActivity_processFrame(JNIEnv* env, jobject, jbyteArray input, jint width, jint height) {
	jbyte* inputBytes = env->GetByteArrayElements(input, nullptr);
	cv::Mat yuv(height + height / 2, width, CV_8UC1, (unsigned char*)inputBytes);
	cv::Mat bgr;
	cv::cvtColor(yuv, bgr, cv::COLOR_YUV2BGR_NV21);
	cv::Mat edges;
	cv::Canny(bgr, edges, 100, 200);
	cv::Mat out;
	cv::cvtColor(edges, out, cv::COLOR_GRAY2BGR);
	std::vector<uchar> buf;
	cv::imencode(".png", out, buf);
	jbyteArray result = env->NewByteArray(buf.size());
	env->SetByteArrayRegion(result, 0, buf.size(), reinterpret_cast<jbyte*>(buf.data()));
	env->ReleaseByteArrayElements(input, inputBytes, JNI_ABORT);
	return result;
}