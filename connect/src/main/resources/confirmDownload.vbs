Dim result
result = MsgBox("Voce gostaria de baixar a JDK antes de continuar ?", vbYesNo + vbQuestion, "Instalar JDK")
If result = vbYes Then
    WScript.Quit(0)
Else
    WScript.Quit(1)
End If
