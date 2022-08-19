package com.cumulus.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

/**
 * excel解析
 *
 * @author zhangxq
 */
@Slf4j
public class ExcelResolve {

    public static final String XLSX = ".xlsx";
    public static final String XLS = ".xls";

    public ExcelResolve() {
    }

    public JSONArray readExcel(MultipartFile file) throws Exception {
        int res = this.checkFile(file);
        if (res == 0) {
            log.warn("非excel文件类型");
            throw new Exception("非excel文件类型");
        } else if (res == 1) {
            return this.readXLSX(file.getInputStream(), 1);
        } else if (res == 2) {
            return this.readXLS(file.getInputStream(), 1);
        } else {
            return new JSONArray();
        }
    }

    /**
     * 多行表头读取
     *
     * @param file
     * @param header 表头所占行数
     * @return
     * @throws Exception
     */
    public JSONArray readExcel(MultipartFile file, int header) throws Exception {
        int res = this.checkFile(file);
        if (res == 0) {
            log.warn("非excel文件类型");
            throw new Exception("非excel文件类型");
        } else if (res == 1) {
            return this.readXLSX(file.getInputStream(), header);
        } else if (res == 2) {
            return this.readXLS(file.getInputStream(), header);
        } else {
            return new JSONArray();
        }
    }

    public int checkFile(MultipartFile file) {
        if (file == null) {
            return 0;
        } else {
            if (StringUtils.endsWith(file.getOriginalFilename(), XLSX)) {
                return 1;
            } else {
                return StringUtils.endsWith(file.getOriginalFilename(), XLS) ? 2 : 3;
            }
        }
    }

    public JSONArray readXLSX(InputStream inputStream, int header) throws IOException {
        Workbook book = new XSSFWorkbook(inputStream);
        Sheet sheet = book.getSheetAt(0);
        return this.read(sheet, book, header);
    }

    public JSONArray readXLS(InputStream inputStream, int header) throws IOException {
        POIFSFileSystem poifsFileSystem = new POIFSFileSystem(inputStream);
        Workbook book = new HSSFWorkbook(poifsFileSystem);
        Sheet sheet = book.getSheetAt(0);
        return this.read(sheet, book, header);
    }

    /**
     * 多行表头读取
     *
     * @param sheet
     * @param book
     * @param header 表头所占行数
     * @return
     * @throws IOException
     */
    public JSONArray read(Sheet sheet, Workbook book, int header) throws IOException {
        int rowStart = sheet.getFirstRowNum();
        int rowEnd = sheet.getLastRowNum();
        if (rowStart == rowEnd) {
            book.close();
            return new JSONArray();
        } else {
            rowStart = header - 1;
            Row firstRow = sheet.getRow(rowStart);
            int cellStart = 0;
//            int cellStart = firstRow.getFirstCellNum();
            int cellEnd = firstRow.getLastCellNum();
            Map<Integer, String> keyMap = new HashMap<>();

            for (int j = cellStart; j < cellEnd; ++j) {
                keyMap.put(j, this.getValue(firstRow.getCell(j), rowStart, j, book, false));
                //如果key为空则取第一行
                if ("".equals(keyMap.get(j))) {
                    keyMap.put(j, this.getValue(sheet.getRow(0).getCell(j), 0, j, book, false));
                }
            }

            JSONArray array = new JSONArray();

            for (int i = rowStart + 1; i <= rowEnd; ++i) {
                Row eachRow = sheet.getRow(i);
                JSONObject obj = new JSONObject();
                StringBuilder sb = new StringBuilder();

                for (int k = cellStart; k < cellEnd; ++k) {
                    if (eachRow != null) {
                        String val = this.getValue(eachRow.getCell(k), i, k, book, false);
                        sb.append(val);
                        obj.put(keyMap.get(k), val);
                    }
                }

                if (sb.toString().length() > 0) {
                    array.add(obj);
                }
            }

            book.close();
            return array;
        }
    }

    public String getValue(Cell cell, int rowNum, int index, Workbook book, boolean isKey) throws IOException {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            String value;
            if (cell.getCellType() == CellType.NUMERIC) {
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return df.format(date);
                } else {
                    cell.setCellType(CellType.STRING);
                    value = cell.getStringCellValue();
                    return value;
                }
            } else if (cell.getCellType() == CellType.STRING) {
                value = cell.getStringCellValue();
                if (value != null && value.trim().length() != 0) {
                    return value.trim();
                } else {
                    if (book != null) {
                        book.close();
                    }

                    return "";
                }
            } else if (cell.getCellType() == CellType.FORMULA) {
                try {
                    value = String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException var12) {
                    value = String.valueOf(cell.getRichStringCellValue());
                }

                return value;
            } else {
                return cell.getCellType() == CellType.BOOLEAN ? cell.getBooleanCellValue() + "" : "";
            }
        } else if (isKey) {
            book.close();
            Object[] var10003 = new Object[2];
            ++rowNum;
            var10003[0] = rowNum;
            ++index;
            var10003[1] = index;
            throw new NullPointerException(String.format("the key on row %s index %s is null ", var10003));
        } else {
            return "";
        }
    }

    public void writeExcel(OutputStream outputStream, String sheetName, List<String> titles, List<LinkedHashMap<String, String>> values) throws IOException {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet;
        if (StringUtils.isEmpty(sheetName)) {
            sheet = workbook.createSheet();
        } else {
            sheet = workbook.createSheet(sheetName);
        }

        sheet.setDefaultColumnWidth(15);
        Map<String, CellStyle> styles = this.createStyles(workbook);
        Row row = sheet.createRow(0);
        LinkedHashMap<String, Integer> titleOrder = new LinkedHashMap<>();

        for (int i = 0; i < titles.size(); ++i) {
            Cell cell = row.createCell(i);
            cell.setCellStyle((CellStyle) styles.get("header"));
            String title = (String) titles.get(i);
            cell.setCellValue(title);
            titleOrder.put(title, i);
        }

        Iterator<LinkedHashMap<String, String>> iterator = values.iterator();

        for (int index = 1; iterator.hasNext(); ++index) {
            row = sheet.createRow(index);
            Map<String, String> value = (Map) iterator.next();
            Iterator var13 = value.entrySet().iterator();

            while (var13.hasNext()) {
                Map.Entry<String, String> map = (Map.Entry) var13.next();
                String title = (String) map.getKey();
                int i = (Integer) titleOrder.get(title);
                Cell cell = row.createCell(i);
                if (index % 2 == 1) {
                    cell.setCellStyle((CellStyle) styles.get("cellA"));
                } else {
                    cell.setCellStyle((CellStyle) styles.get("cellB"));
                }

                Object object = map.getValue();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (object instanceof Double) {
                    cell.setCellValue((Double) object);
                } else if (object instanceof Date) {
                    String time = simpleDateFormat.format((Date) object);
                    cell.setCellValue(time);
                } else if (object instanceof Calendar) {
                    Calendar calendar = (Calendar) object;
                    String time = simpleDateFormat.format(calendar.getTime());
                    cell.setCellValue(time);
                } else if (object instanceof Boolean) {
                    cell.setCellValue((Boolean) object);
                } else if (object != null) {
                    cell.setCellValue(object.toString());
                }
            }
        }

        for (int k = 0; k < values.size(); ++k) {
            sheet.autoSizeColumn(k);
        }

        this.setSizeColumn((HSSFSheet) sheet, values.size());

        try {
            workbook.write(outputStream);
        } catch (IOException var25) {
            var25.printStackTrace();
            throw var25;
        } finally {
            if (workbook != null) {
                workbook.close();
            }

        }

    }

    private void setSizeColumn(HSSFSheet sheet, int size) {
        for (int columnNum = 0; columnNum < size; ++columnNum) {
            int columnWidth = sheet.getColumnWidth(columnNum) / 256;

            for (int rowNum = 0; rowNum < sheet.getLastRowNum(); ++rowNum) {
                HSSFRow currentRow;
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }

                if (currentRow.getCell(columnNum) != null) {
                    HSSFCell currentCell = currentRow.getCell(columnNum);
                    if (currentCell.getCellType() == CellType.STRING) {
                        int length = currentCell.getStringCellValue().getBytes().length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
            }

            sheet.setColumnWidth(columnNum, columnWidth * 256);
        }

    }

    private Map<String, CellStyle> createStyles(Workbook wb) {
        Map<String, CellStyle> styles = new HashMap();
        HSSFCellStyle titleStyle = (HSSFCellStyle) wb.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setLocked(true);
        titleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);
        titleFont.setFontName("微软雅黑");
        titleStyle.setFont(titleFont);
        styles.put("title", titleStyle);
        HSSFCellStyle headerStyle = (HSSFCellStyle) wb.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillPattern(FillPatternType.NO_FILL);
        headerStyle.setWrapText(true);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        Font headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        titleFont.setFontName("微软雅黑");
        headerStyle.setFont(headerFont);
        styles.put("header", headerStyle);
        Font cellStyleFont = wb.createFont();
        cellStyleFont.setFontHeightInPoints((short) 10);
        cellStyleFont.setColor(IndexedColors.BLACK.getIndex());
        cellStyleFont.setFontName("微软雅黑");
        HSSFCellStyle cellStyleA = (HSSFCellStyle) wb.createCellStyle();
        cellStyleA.setAlignment(HorizontalAlignment.LEFT);
        cellStyleA.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyleA.setWrapText(true);
        cellStyleA.setBorderRight(BorderStyle.THIN);
        cellStyleA.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setBorderLeft(BorderStyle.THIN);
        cellStyleA.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setBorderTop(BorderStyle.THIN);
        cellStyleA.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setBorderBottom(BorderStyle.THIN);
        cellStyleA.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setFont(cellStyleFont);
        cellStyleA.setWrapText(false);
        styles.put("cellA", cellStyleA);
        HSSFCellStyle cellStyleB = (HSSFCellStyle) wb.createCellStyle();
        cellStyleB.setAlignment(HorizontalAlignment.LEFT);
        cellStyleB.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyleB.setWrapText(true);
        cellStyleB.setBorderRight(BorderStyle.THIN);
        cellStyleB.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setBorderLeft(BorderStyle.THIN);
        cellStyleB.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setBorderTop(BorderStyle.THIN);
        cellStyleB.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setBorderBottom(BorderStyle.THIN);
        cellStyleB.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setFont(cellStyleFont);
        cellStyleA.setWrapText(false);
        styles.put("cellB", cellStyleB);
        return styles;
    }

    /**
     * 解析excel文件
     *
     * @param fileInputStream 文件输入流
     * @param resolveExcelRow 解析每行的数据
     */
    public static void resolveExcel(InputStream fileInputStream, ResolveExcelRow resolveExcelRow) throws IOException {
        Workbook workbook = WorkbookFactory.create(fileInputStream);
        int sheetsSize = workbook.getNumberOfSheets();
        for (int i = 0; i < sheetsSize; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            int rowSize = sheet.getPhysicalNumberOfRows();
            if (rowSize > 1) {
                Map<String, Integer> headMap = getHeadMap(sheet.getRow(0));
                for (int j = 1; j < rowSize; j++) {
                    Row row = sheet.getRow(j);
                    resolveExcelRow.resolveRow(headMap, row);
                }
            }
        }
    }

    /**
     * 生成表头Map 第一行数据 key:每行的数据 value:第几行
     *
     * @param row 第一行的数据
     */
    public static Map<String, Integer> getHeadMap(Row row) {
        int filedSize = row.getPhysicalNumberOfCells();
        Map<String, Integer> result = new HashMap<>(filedSize);
        for (int i = 0; i < filedSize; i++) {
            String filedStr = row.getCell(i).getStringCellValue();
            result.put(filedStr, i);
        }
        return result;
    }

    /**
     * 获取单元格内的数据返回字符串
     *
     * @param cell 单元格
     * @return 返回字符串
     */
    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return DateUtils.SIMPLE_DFY_MD_HMS.format(cell.getDateCellValue());
                } else {
                    return Double.toString(cell.getNumericCellValue());
                }
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            case ERROR:
                return "非法字符";
            default:
                return "未知类型";
        }
    }

    /**
     * 解析每行数据
     */
    public interface ResolveExcelRow {

        /**
         * 解析
         *
         * @param headMap 表头的map 第一行数据 key:每行的数据 value:第几行
         * @param row     行的内容
         */
        void resolveRow(Map<String, Integer> headMap, Row row);
    }
}
