Sub 合并当前目录下所有Excel的Sheet到当前打开的Excel对应Sheet中()
    ' 功能说明：
    ' 将当前目录下的所有Excel文件（下面简称“被合并的workbook”）的所有sheet全部合并到当前打开的Excel文件（下面简称“目标workbook”）的sheet1中

    Dim workingDir, processingWorkbookName, activeWorkbookName
    Dim processingWorkbook As Workbook, processedWorkbookNames As String
    Dim sheetIndex As Long
    Dim processedWorkbookCount As Long
    Application.ScreenUpdating = False
    workingDir = ActiveWorkbook.Path
    
    ' 取得当前目录下的Excel文件路径
    processingWorkbookName = Dir(workingDir & "\" & "*.xlsx")
    
    ' 当前打开的Excel文件名称，即目标workbook文件名
    activeWorkbookName = ActiveWorkbook.Name
    
    ' 记录总计被合并的Excel文件数
    processedWorkbookCount = 0
    Do While processingWorkbookName <> ""
        If processingWorkbookName <> activeWorkbookName Then
            ' 打开下一个被合并的workbook
            Set processingWorkbook = Workbooks.Open(workingDir & "\" & processingWorkbookName)
            processedWorkbookCount = processedWorkbookCount + 1
            
            ' 将上下文设置为目标workbook的第1个sheet
            With Workbooks(1).ActiveSheet
                ' 在目标workbook的第1个sheet的最后一行，写入当前被合并workbook的文件名
                .Cells(.Range("A65536").End(xlUp).Row + 2, 1) = Left(processingWorkbookName, Len(processingWorkbookName) - 4)
                ' 循环复制被合并workbook的所有sheet到目标workbook的第1个sheet中
                For sheetIndex = 1 To Sheets.Count
                    processingWorkbook.Sheets(sheetIndex).UsedRange.Copy .Cells(.Range("A65536").End(xlUp).Row + 1, 1)
                Next
                ' 记录被合并的workbook文件名
                processedWorkbookNames = processedWorkbookNames & Chr(13) & processingWorkbook.Name
                processingWorkbook.Close False
            End With
        End If
        
        ' 取得当前目录下的下一个Excel文件路径
        processingWorkbookName = Dir
    Loop
    Range("A1").Select
    Application.ScreenUpdating = True
    MsgBox "共合并了" & processedWorkbookCount & "个工作薄下的全部工作表。如下：" & Chr(13) & processedWorkbookNames, vbInformation, "提示"
End Sub

