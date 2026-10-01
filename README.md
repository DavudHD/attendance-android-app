# نظام حضور وانصراف الموظفين

هذا هو مشروع Android لتطبيق حضور وانصراف الموظفين في مواعيد العمل الرسمية.

## المميزات
- تسجيل حضور الموظف
- تسجيل انصراف الموظف
- حالة الحضور: حاضر / متأخر / انصراف مبكر
- حفظ السجلات محليًا داخل التطبيق
- واجهة عربية

## متطلبات التشغيل
- Android Studio Iguana أو أحدث
- JDK 17
- Android SDK 34

## إنشاء ملف التوقيع
1. انسخ الملف `key.properties.example` إلى `key.properties`
2. أنشئ مجلد `keystore` في جذر المشروع
3. أنشئ ملف keystore محليًا عبر الأمر التالي:

```bash
keytool -genkeypair -v \
  -keystore keystore/attendance-release-key.jks \
  -keyalias attendance-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass 123456 \
  -keypass 123456 \
  -dname "CN=Attendance App, OU=IT, O=Company, L=City, S=State, C=US"
```

4. عدّل قيم `key.properties` لتطابق ملف keystore الذي تم إنشاؤه.

## بناء ملف APK
بعد إعداد ملف التوقيع، نفّذ الأمر التالي:

### Windows
```bash
gradlew.bat assembleRelease
```

### macOS/Linux
```bash
./gradlew assembleRelease
```

سيتم إنشاء الملف في المسار التالي:
- `app/build/outputs/apk/release/`

## ملاحظات
هذا المشروع يمثل نسخة MVP أولية (Minimum Viable Product)، ويمكن تطويره لاحقًا بإضافة:
- صلاحيات المدير والموظف
- Firebase أو API
- الموقع GPS
- التقارير الشهرية
- الإجازات والغياب

## المالك
تم إنشاء هذا المشروع داخل مستودع GitHub في الحساب: `DavudHD`
