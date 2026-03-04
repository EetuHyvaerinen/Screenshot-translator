# ScreenTranslator

![MIT License](https://img.shields.io/badge/License-MIT-green)

ScreenTranslator is a lightweight Java tool that lets you select an area of your screen, extract text using OCR and translate it into English, displayed above the selected area

---

## Features

- Capture any screen area using a global hotkey (Ctrl+Alt+S)
- OCR support for multiple scripts, currently enabled: Cyrillic, Latin, Arabic. More scripts can be added through Tesseract and adding the script names to the OCR_LANGUAGES
- Translate detected text to English using Google Translate (currently an unofficial http endpoint)
- Displays translation above the selection area
- Tray icon for quick access and exit
- Lightweight and easy to use

---

### Prerequisites

- **Java 17+**  
  Check your Java version:

```bash
java -version
```

- **Tesseract OCR**  
  ScreenTranslator uses Tesseract for text recognition. Install it according to your OS:

  - **Windows:** [Download from GitHub](https://github.com/tesseract-ocr/tesseract)  
  - **Linux/macOS:**  
        # Linux (Ubuntu/Debian)
        sudo apt install tesseract-ocr

        # macOS (Homebrew)
        brew install tesseract

- **Set the `TESSDATA_PREFIX` environment variable**  
  Point it to the `tessdata` folder of your Tesseract installation:

  - **Windows (PowerShell):**

        setx TESSDATA_PREFIX "C:\Program Files\Tesseract-OCR\tessdata"

  - **Linux/macOS:**

        export TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata

This ensures OCR can find the language data files correctly

## Installation
Clone the repository:

```bash
git clone https://github.com/EetuHyvaerinen/ScreenTranslator.git
cd ScreenTranslator
```
Build and run with gradle:
```bash
./gradlew build
./gradlew run
```
Alternatively, you can build a standalone JAR:

```bash
./gradlew jar
java -jar build/libs/ScreenTranslator.jar
```
## Usage

1. Launch ScreenTranslator
2. Press Ctrl + Alt + S to start a selection
3. Click and drag to select a region of the screen containing the text you want to translate
4. Release the mouse button to capture
5. The translated text will appear above the selection in English
6. Right-click or press Esc to cancel selection
