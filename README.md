<div dir="rtl">

  <h1 align="center">📱 TaskFlow – Smart Task Manager App</h1>

  <p align="center">
    מנהל משימות חכם למובייל עם <strong>אוטומציה של סדר עדיפויות וקטגוריות</strong>.<br>
    כולל מסכי Splash, Login, Register, Dashboard ו-HomeScreen להצגת משימות.<br>
    מבוסס Jetpack Compose + MVVM.
  </p>

  <br>
  <p align="center">
    <img src="https://img.shields.io/badge/Kotlin-100%25-orange?logo=kotlin" alt="Kotlin Badge">
    <img src="https://img.shields.io/badge/Jetpack%20Compose-UI-blue?logo=jetpackcompose" alt="Jetpack Compose Badge">
    <img src="https://img.shields.io/badge/Architecture-MVVM-green" alt="MVVM Badge">
    <img src="https://img.shields.io/badge/License-MIT-blue" alt="License Badge">
  </p>

  <br>

  <h2 align="center">🎬 Demo / המחשה</h2>

  <br/>

  <table>
  <tr>
    <td align="center">
      <img src="assets/taskflow_image4.PNG" width="350" alt="מסך הרשמה" />
      <br><b>מסך הרשמה</b>
    </td>
    <td align="center">
      <img src="assets/taskflow_image2.PNG" width="350" alt="מסך התחברות" />
      <br><b>מסך התחברות</b>
    </td>
    <td align="center">
      <img src="assets/taskflow_image1.PNG" width="350" alt="מסך בית" />
      <br><b>מסך בית</b>
    </td>
    <td align="center">
      <img src="assets/taskflow_image3.PNG" width="350" alt="מסך משימות" />
      <br><b>מסך משימות</b>
    </td>
  </tr>
</table>

<br/>

  <hr>

  <h2 align="center">🔎 Overview</h2>
  <p align="center">
    <strong>TaskFlow</strong> נועדה לעזור לך לנהל משימות בצורה חכמה, עם יכולות סיווג אוטומטיות
    וסדר עדיפויות מבוסס הקשר. <br>
    מסך <strong>Dashboard</strong> מציג את שם המשתמש, כמות המשימות לביצוע וסינון לפי קטגוריות, <br>
    ו-<strong>HomeScreen</strong> מציג את כל המשימות לבחירה, עריכה וסיום משימות.
  </p>

  <hr>

  <h2 align="center">🚀 Features</h2>

  <table align="center">
    <thead>
      <tr>
        <th>תחום</th>
        <th>תכונה</th>
        <th>סטטוס</th>
        <th>הערות</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>🖥️ מסכים</td>
        <td>SplashScreen,
          LoginScreen,
          RegisterScreen,
          Dashboard,
          HomeScreen</td>
        <td>✅</td>
        <td>מעבר חכם בין מסכים + הצגת משימות לפי קטגוריות</td>
      </tr>
      <tr>
        <td>📝 משימות</td>
        <td>הוספה, עריכה ומחיקה</td>
        <td>✅</td>
        <td>CRUD מלא</td>
      </tr>
      <tr>
        <td>⭐ חשיבות</td>
        <td>סימון כחשוב / הושלם</td>
        <td>✅</td>
        <td>טוגל מהיר</td>
      </tr>
      <tr>
        <td>⚡ אוטומציה</td>
        <td>סדר עדיפויות לפי תאריך יעד, חשיבות, מילות מפתח</td>
        <td>✅</td>
        <td>Rule-based</td>
      </tr>
      <tr>
        <td>🏷️ קטגוריות</td>
        <td>בית 🏠, עבודה 💼, לימודים 🎓, חשבונות 🧾, קניות 🛒, ספורט 🏃‍♂️, בריאות 🩺, פרויקטים אישיים 💡, תחבורה 🚗, משפחה 👨‍👩‍👧‍👦,, ואחרים 🏷️</td>
        <td>✅</td>
        <td>סינון אוטומטי בדשבורד ו-Homescreen</td>
      </tr>
      <tr>
        <td>🔍 חיפוש</td>
        <td>לפי כותרת / תיאור</td>
        <td>✅</td>
        <td>ריל־טיים</td>
      </tr>
      <tr>
        <td>📅 קיבוץ תאריכים</td>
        <td>היום, מחר, השבוע, עתידי, ללא תאריך</td>
        <td>✅</td>
        <td>אוטומטי</td>
      </tr>
      <tr>
        <td>📦 ארכיון</td>
        <td>משימות שהושלמו (פתיחה/סגירה)</td>
        <td>✅</td>
        <td>Collapsible section</td>
      </tr>
      <tr>
        <td>👆 מחוות</td>
        <td>לחיצה על כפתור לסיום או מחיקה</td>
        <td>✅</td>
        <td>Gesture control</td>
      </tr>
      <tr>
        <td>📄 דיאלוג פריט</td>
        <td>כותרת, תיאור, תאריך יעד, חשיבות</td>
        <td>✅</td>
        <td>תצוגה מפורטת</td>
      </tr>
    </tbody>
  </table>

  <hr>

  <div dir="rtl">
  <h2>🛠️ טכנולוגיות</h2>
  <ul>
    <li><strong>שפת פיתוח:</strong> Kotlin</li>
    <li><strong>סביבת פיתוח (IDE):</strong> Android Studio</li>
    <li><strong>ממשק משתמש (UI):</strong> Jetpack Compose + Material 3</li>
    <li><strong>ארכיטקטורה:</strong> MVVM</li>
    <li><strong>ניהול נתונים:</strong> Room / ViewModel / StateFlow</li>
    <li><strong>אימות משתמשים:</strong> Firebase Auth</li>
  </ul>
</div>

  <hr>

  <h2>📄 רישיון</h2>
  <p>
    הפרויקט מופץ תחת רישיון <strong>MIT</strong> – חופשי לשימוש, שינוי והפצה, כל עוד נשמר קרדיט למחבר.
  </p>
  <p>למידע נוסף ראה את קובץ <a href="LICENSE">LICENSE</a></p>

  <hr>

  <p align="center"><strong>👨‍💻 Raz Eini (2025)</strong></p>

</div>
