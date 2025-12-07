package ng.com.createsoftware.fn_accounting_service.batch;

import io.swagger.v3.oas.models.links.Link;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class BatchParser {
    public static List<Map<String, String>> parseCsv(MultipartFile file)throws IOException{
        List<Map<String, String>> rows = new ArrayList<>();
        try(Reader r = new InputStreamReader(file.getInputStream())){
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(r);
            for(CSVRecord record : records){
                Map<String, String> map = new LinkedHashMap<>();
                record.toMap().forEach(map::put);
                rows.add(map);
            }
        }
        return rows;
    }

    public static List<Map<String, String>> parseXlsx(MultipartFile file) throws IOException{
        List<Map<String, String>> rows = new ArrayList<>();
        try(InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)){
            Sheet sheet = wb.getSheetAt(0);
            Iterator<Row> it = sheet.iterator();
            if(!it.hasNext()) return rows;

            Row header = it.next();
            List<String> columns = new ArrayList<>();
            header.forEach(c -> columns.add(c.getStringCellValue()));

            while(it.hasNext()){
                Row r = it.next();
                Map<String, String> m = new LinkedHashMap<>();
                for(int i = 0; i < columns.size(); i++){
                    Cell cell = r.getCell(i);
                    String val = "";
                    if(cell != null){
                        switch (cell.getCellType()){
                            case STRING -> val = cell.getStringCellValue();
                            case NUMERIC -> val = String.valueOf(cell.getNumericCellValue());
                            case BOOLEAN -> val = String.valueOf(cell.getBooleanCellValue());
                            default -> val = "";
                        }
                    }
                    m.put(columns.get(i), val);
                }
                rows.add(m);
            }
        }
        return rows;
    }

    public static List<Map<String,String>> parse(MultipartFile file) throws  IOException{
        String name= file.getOriginalFilename();
        if(name == null) throw new IllegalArgumentException("Empty file name");
        if(name.endsWith(".csv")) return  parseCsv(file);
        if(name.endsWith(".xlsx") || name.endsWith(".xls")) return parseXlsx(file);
        throw new IllegalArgumentException("Unsupported file type.");
    }
}
