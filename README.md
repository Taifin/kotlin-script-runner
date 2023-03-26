# Kotlin script runner

This is a simple GUI application that allows user to enter some Kotlin source code in editor and then run it
using `kotlinc`. This is a test task for JetBrains Internship.

## Implemented features

* _Code editor with simple syntax highlighting:_ some keywords are highlighted with red color
* _Output of the script:_ both errors and usual output are redirected to user's interface
* _Script manager:_ allows to save and upload scripts to avoid writing them from scratch every time.
* _Time estimations:_ approximate running time of the scripts is being recorded and then used to estimate execution time
  of the next run.

## Running the runner

To run the application, use the following commands:

```bash
./gradlew build
./gradlew run
```

Type your code in `Kotlin Code` pane or upload new script through the corresponding button. You may also save your code
via `Save` button. All files that were saved or uploaded are shown on the right side of the editor pane, you can upload
them into editor by clicking.

After you are done with editing, press `Run` button to run the script. While script is running, the button will be
unavailable. After the first run, you can see estimated time and progress at the corresponding section.

## Remarks

* Despite ability to upload new files, it is currently impossible to delete script files from the application. Please,
  delete them manually if needed.
* Output pane can freeze if contains a lot of text.
* By default, any script that does not produce output will be terminated after 60 seconds. Timeout can be changed
  in `Runner` properties.