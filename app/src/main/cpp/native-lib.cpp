#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_appforpills_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string schedule = "SCHEDULE";
    return env->NewStringUTF(schedule.c_str());
}
