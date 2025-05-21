package com.sky;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-21
 * Time: 21:26
 */
public class POITest {

    private static void write(){
        // 1. 创建一个Excel工作簿
        Workbook workbook = new XSSFWorkbook();

        // 2. 在工作簿中创建一个名为"info"的sheet
        Sheet sheet = workbook.createSheet("info");

        // 3. 创建第2行（索引从0开始，所以是index=1），并在该行创建两个单元格，内容为“姓名”和“城市”，下标分别为5和6
        Row headerRow = sheet.createRow(1);
        Cell nameHeaderCell = headerRow.createCell(5);
        nameHeaderCell.setCellValue("姓名");
        Cell cityHeaderCell = headerRow.createCell(6);
        cityHeaderCell.setCellValue("城市");

        // 随机生成姓名和城市的数组
        String[] names = {"张三", "李四", "王五", "赵六", "钱七"};
        String[] cities = {"北京", "上海", "广州", "深圳", "成都"};

        // 4. 创建第3、4行，并在下标为5、6的单元格填入随机姓名和随机城市
        Random random = new Random();
        for (int i = 0; i < 2; i++) {
            Row dataRow = sheet.createRow(2 + i); // 第3、4行
            Cell nameCell = dataRow.createCell(5);
            nameCell.setCellValue(names[random.nextInt(names.length)]);
            Cell cityCell = dataRow.createCell(6);
            cityCell.setCellValue(cities[random.nextInt(cities.length)]);
        }

        // 5. 将Excel文件通过流写入磁盘路径 D://
        try (FileOutputStream fileOut = new FileOutputStream("D://user.xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Excel文件已成功保存到 D://user.xlsx");
    }

    private static void read(){
        // 1. 指定文件路径
        String filePath = "D://user.xlsx";

        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            // 2. 创建工作簿对象
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // 3. 获取名为 "info" 的 sheet
            Sheet sheet = workbook.getSheet("info");

            if (sheet != null) {
                // 4. 获取最后一行有文字的行号
                int lastRowNum = sheet.getLastRowNum();

                // 5. 从第2行开始循环读取每一行
                for (int i = 1; i <= lastRowNum; i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        // 6. 读取下标为5和6的单元格内容
                        Cell nameCell = row.getCell(5);
                        Cell cityCell = row.getCell(6);

                        // 7. 输出单元格内容
                        String name = nameCell != null ? nameCell.getStringCellValue() : "";
                        String city = cityCell != null ? cityCell.getStringCellValue() : "";
                        System.out.println(name + " " + city);
                    }
                }
            } else {
                System.out.println("Sheet 'info' 不存在");
            }
        } catch (IOException e) {
            throw new RuntimeException("读取Excel文件时发生错误", e);
        }
    }

    public static void main(String[] args) {
        write();
        read();
    }
}
