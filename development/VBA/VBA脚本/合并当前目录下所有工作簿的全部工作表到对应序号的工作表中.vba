Sub 合并当前目录下所有工作簿的全部工作表到对应序号的工作表中()
    ' 功能说明：
    ' 将当前目录下的所有Excel文件（下面简称“被合并的workbook”）的所有sheet全部合并到当前打开的Excel文件（下面简称“目标workbook”）的对应sheet中
    ' 例如：当前目录下有三个Excel文件，分别是：1.xlsx、2.xlsx、3.xlsx；其中：
    '           1.xlsx和2.xlsx文件都有3个sheet，分别为：sheet1、sheet2、sheet3，
    '           3.xlsx文件有sheet1、sheet2、sheet3、sheet4
    '      如果在1.xlsx文件中执行当前的程序，最终的结果将是：
    '           1.xlsx的sheet1中的内容为原来1.xlsx、2.xlsx和3.xlsx的sheet1中的内容合在一起
    '           1.xlsx的sheet2中的内容为原来1.xlsx、2.xlsx和3.xlsx的sheet2中的内容合在一起
    '           1.xlsx的sheet3中的内容为原来1.xlsx、2.xlsx和3.xlsx的sheet3中的内容合在一起
    '           1.xlsx新建sheet4，内容为原来3.xlsx的sheet4中的内容合在一起

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
            For sheetIndex = 1 To processingWorkbook.Sheets.Count
                ' 如果目标workbook对应sheetIndex的sheet不存在，增加一个sheet
                If Workbooks(1).Sheets.Count < sheetIndex Then
                    Workbooks(1).Sheets.Add after:=Workbooks(1).Sheets(sheetIndex - 1)
                End If
                With Workbooks(1).Sheets(sheetIndex)
                    ' 在目标workbook的sheet的最后一行，写入当前被合并workbook的文件名和sheet名。如果不需要插入来源Excel的名称，注释下面一行即可。
                    .Cells(.Range("A65536").End(xlUp).Row + 2, 1) = "从[" + Left(processingWorkbookName, Len(processingWorkbookName) - 4) + processingWorkbook.Sheets(sheetIndex).Name + "]复制过来"
                    processingWorkbook.Sheets(sheetIndex).UsedRange.Copy .Cells(.Range("A65536").End(xlUp).Row + 1, 1)
                End With
            Next
            ' 记录被合并的workbook文件名
            processedWorkbookNames = processedWorkbookNames & Chr(13) & processingWorkbook.Name
            processingWorkbook.Close False
        End If
        
        ' 取得当前目录下的下一个Excel文件路径
        processingWorkbookName = Dir
    Loop
    Application.ScreenUpdating = True
    MsgBox "共合并了" & processedWorkbookCount & "个工作薄下的全部工作表。如下：" & Chr(13) & processedWorkbookNames, vbInformation, "提示"
End Sub



