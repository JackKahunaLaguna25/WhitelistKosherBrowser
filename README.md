# 🌐 Whitelist Kosher Browser  

**Language / שפה:** [עברית](#עברית-il) | [English](#english-en)

---

## עברית IL

### 📖 תיאור
**דפדפן Whitelist Kosher Browser** הוא דפדפן כשר עם מערכת סינון מבוססת רשימת לבנה –  
כלומר, רק אתרים שמופיעים ברשימה הלבנה יהיו נגישים.  

- קישור לדף: [Whitelist Kosher Browser](https://github.com/JackKahunaLaguna25/WhitelistKosherBrowser/)  
- מטרה: זיכוי הרבים – דפדפן כשר עם סינון מבוסס רשימת לבנה  

---

### 💾 איך מתקינים את התוכנה
1. נכנסים לעמוד ה-Releases של הפרויקט ב-GitHub ומורידים את קובץ ה-APK.או ישירות: [הורד APK](https://github.com/JackKahunaLaguna25/WhitelistKosherBrowser/releases/download/v0.0.1/WhitelistKosherBrowser.apk) 
2. מעבירים את קובץ ה-APK לטלפון
3. מתקינים את ה-APK על הטלפון (ייתכן שתצטרכו לאשר התקנה ממקור לא מוכר).  
4. מפעילים את הדפדפן – בשלב זה כל האתרים חסומים עד שתיטען רשימה לבנה.  

---
   
### ⚙️ איך יוצרים רשימת לבנה

‏1. נכנסים לעמוד ה-Releases של הפרויקט ב-GitHub ומורידים את קובץ WhitelistGenerator.exe.
או ישירות: [הורד את WhitelistGenerator.exe](https://github.com/JackKahunaLaguna25/WhitelistKosherBrowser/releases/download/v0.0.1/WhitelistGenerator.exe)

2. פותחים את הקובץ `WhitelistGenerator.exe` (אם נפתח התראה של  Windows לוחצים על מידע נוסף ואז הרץ בכל זאת)

הרשימה הלבנה מחולקת ל-3 קטגוריות עיקריות:

**קטגוריה 1 - אתרים שמאושרים עם כל הסאב-דומיינים והנתיבים (AllowSubDomains)**  
- הסבר: אם אתר כמו `google.com` מאושר, גם כל הסאב-דומיינים שלו (`mail.google.com`) וכל הנתיבים שלו (`google.com/images`) יהיו מאושרים.  
- שימוש מומלץ: מתאים לאתרים שמפנים לנתיבים רבים או סאב-דומיינים, כדי שהאתר יעבוד בלי בעיות.
  
 **קטגוריה 2 - אתרים שמאושרים עם נתיבים אבל בלי סאב-דומיינים (AllowNoSubDomains)**  
- הסבר: אם `google.com` מאושר, גם הנתיבים שלו (`google.com/images`) יהיו מאושרים, אבל סאב-דומיינים כמו `mail.google.com` יהיו חסומים.

**קטגוריה 3 - אתרים שמאושרים רק הכתובת המדויקת (AllowExactOnly)**  
- הסבר: אם `google.com` מאושר, גם הנתיבים (`google.com/images`) וגם הסאב-דומיינים (`mail.google.com`) יהיו חסומים.

> הערה: מאחר שרוב האתרים מפנים לנתיבים ולפעמים לסאב-דומיינים, בדרך כלל כדאי להשתמש בקטגוריה הראשונה או השנייה כדי לוודא שהאתר יעבוד בצורה תקינה.

אין לכלול את תחילת הפרוטוקול לדוגמה, כתובת תקינה:  
`google.com`

כתובות שגויות לדוגמה:  
`https://google.com`  
`www.google.com`

יש להפריד בין כתובות שונות על ידי כתיבה בשורה חדשה, לדוגמה:  
`google.com`  
`duckduckgo.com`  
`bing.com`

3. לוחצים על הכפתור `Generate whitelist.enc` ליצירת הרשימה ושומרים את הקובץ בכל שם שנרצה.
   הקובץ ייווצר עם סיומת: .enc  


---

### 📱 שימוש ברשימה הלבנה בדפדפן
1. מעבירים את קובץ ה-.enc שיצרנו לטלפון.  
2. מתקינים את קובץ ה-APK בטלפון (אם עדיין לא התקנתם).  
3. בתחילה כל הכתובות יהיו חסומות, כי עדיין לא טענו רשימה לבנה.  
4. כדי לטעון רשימה לבנה:  
   - נכנסים לדפדפן בטלפון.  
   - לוחצים על תפריט שלוש הנקודות בחלק העליון.  
   - נכנסים להגדרות (Settings).  
   - לוחצים על Load White List.  
   - בוחרים את קובץ ה-.enc שיצרנו מקודם ע"י WhitelistGenerator.exe.  

✅ מעכשיו רק הכתובות שהוכנסו לרשימה יהיו נגישות.

---

### 📖 פרטים נוספים

הדפדפן הוא מבוסס על גרסה מותאמת של Firefox Focus כך שהוא מציג את האתרים עם המנוע של פיירפוקס עצמו Gecko engine, במקום להשתמש במנוע המובנה של אנדרואיד Android System Webview. תכונה זו של הדפדפן מאפשרת למשתמשים שרוצים להסיר את Android System Webview לגלוש באינטרנט ללא תלותיות ברכיב זה.

---






## English EN
### 📖 Description
**Whitelist Kosher Browser** is a kosher browser with whitelist-based filtering system –  
meaning only websites that appear on the whitelist will be accessible.  
- Project page: [Whitelist Kosher Browser](https://github.com/JackKahunaLaguna25/WhitelistKosherBrowser/)  
- Purpose: A kosher browser with whitelist-based filtering for the community  
---
### 💾 How to Install the Software
1. Go to the project's Releases page on GitHub and download the APK file. Or directly: [Download APK](https://github.com/JackKahunaLaguna25/WhitelistKosherBrowser/releases/download/v0.0.1/WhitelistKosherBrowser.apk) 
2. Transfer the APK file to your phone
3. Install the APK on your phone (you may need to approve installation from unknown sources).  
4. Launch the browser – at this stage all websites are blocked until a whitelist is loaded.  
---
   
### ⚙️ How to Create a Whitelist
1. Go to the project's Releases page on GitHub and download the WhitelistGenerator.exe file.
Or directly: [Download WhitelistGenerator.exe](https://github.com/JackKahunaLaguna25/WhitelistKosherBrowser/releases/download/v0.0.1/WhitelistGenerator.exe)
2. Open the `WhitelistGenerator.exe` file (if a Windows warning appears, click "More info" then "Run anyway")

The whitelist is divided into 3 main categories:

**Category 1 - Websites approved with all subdomains and paths (AllowSubDomains)**  
- Explanation: If a website like `google.com` is approved, all its subdomains (`mail.google.com`) and all its paths (`google.com/images`) will also be approved.  
- Recommended use: Suitable for websites that redirect to many paths or subdomains, so the website works without issues.
  
**Category 2 - Websites approved with paths but without subdomains (AllowNoSubDomains)**  
- Explanation: If `google.com` is approved, its paths (`google.com/images`) will also be approved, but subdomains like `mail.google.com` will be blocked.

**Category 3 - Websites approved for exact address only (AllowExactOnly)**  
- Explanation: If `google.com` is approved, both paths (`google.com/images`) and subdomains (`mail.google.com`) will be blocked.

> Note: Since most websites redirect to paths and sometimes to subdomains, it's usually better to use the first or second category to ensure the website functions properly.

Do not include the protocol prefix. For example, a valid address:  
`google.com`

Invalid addresses for example:  
`https://google.com`  
`www.google.com`

Separate different addresses by writing on a new line, for example:  
`google.com`  
`duckduckgo.com`  
`bing.com`

3. Click the `Generate whitelist.enc` button to create the list and save the file with any name you want.
   The file will be created with the extension: .enc  
---
### 📱 Using the Whitelist in the Browser
1. Transfer the .enc file we created to your phone.  
2. Install the APK file on your phone (if you haven't installed it yet).  
3. Initially all addresses will be blocked, because we haven't loaded a whitelist yet.  
4. To load a whitelist:  
   - Open the browser on your phone.  
   - Click on the three-dot menu at the top.  
   - Go to Settings.  
   - Click on Load White List.  
   - Choose the .enc file we created earlier using WhitelistGenerator.exe.

✅ From now on, only the addresses entered in the list will be accessible.
---
### 📖 Additional Details
The browser is based on a customized version of Firefox Focus, so it displays websites with Firefox's own Gecko engine, instead of using Android's built-in Android System Webview engine. This browser feature allows users who want to remove Android System Webview to browse the internet without dependencies on this component.
