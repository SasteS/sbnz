//package com.ftn.sbnz.service;
//
//import org.drools.template.ObjectDataCompiler;
//import org.kie.api.KieServices;
//import org.kie.api.builder.*;
//import org.kie.api.runtime.KieContainer;
//import org.kie.api.runtime.KieSession;
//import org.springframework.stereotype.Service;
//
//import java.io.InputStream;
//import java.util.*;
//
//import com.ftn.sbnz.model.models.Machine;
//
//@Service
//public class TemplateService {
//
//    private final KieContainer kieContainer;
//
//    public TemplateService(KieContainer kieContainer) {
//        this.kieContainer = kieContainer;
//    }
//
//    public void runTemplateExample() throws Exception {
//        // 1. Load the .drt template file
//        InputStream templateStream = getClass().getResourceAsStream("/rules/template/machine-template.drt");
//
//        // 2. Prepare the data rows for the template
//        List<Map<String, Object>> data = new ArrayList<>();
//
//        Map<String, Object> row1 = new HashMap<>();
//        row1.put("minTemp", 0);
//        row1.put("maxTemp", 40);
//        row1.put("label", "Low Temp");
//        data.add(row1);
//
//        Map<String, Object> row2 = new HashMap<>();
//        row2.put("minTemp", 40);
//        row2.put("maxTemp", 80);
//        row2.put("label", "Normal Temp");
//        data.add(row2);
//
//        Map<String, Object> row3 = new HashMap<>();
//        row3.put("minTemp", 80);
//        row3.put("maxTemp", 100);
//        row3.put("label", "High Temp");
//        data.add(row3);
//
//        // 3. Compile template + data into DRL
//        ObjectDataCompiler compiler = new ObjectDataCompiler();
//        String drl = compiler.compile(data, templateStream);
//        System.out.println("Generated DRL from Template:\n" + drl);
//
//        // 4. Build a temporary KieSession from generated DRL
//        KieServices ks = KieServices.Factory.get();
//        KieFileSystem kfs = ks.newKieFileSystem();
//        kfs.write("src/main/resources/rules/generated/machine-template-generated.drl", drl);
//        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
//        KieContainer kc = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
//        KieSession ksession = kc.newKieSession();
//
//        // 5. Insert sample data
//        Machine m1 = new Machine("M1", "Pump A");
//        m1.setTemperature(85.0);
//        ksession.insert(m1);
//
//        // 6. Fire rules
//        ksession.fireAllRules();
//        ksession.dispose();
//    }
//}


package com.ftn.sbnz.service;

import com.ftn.sbnz.model.models.Machine;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class TemplateService {

    private final KieServices kieServices = KieServices.Factory.get();
    private final KieFileSystem kieFileSystem;
    private KieContainer kieContainer;

    public TemplateService() {
        this.kieFileSystem = kieServices.newKieFileSystem();
        rebuildContainer(); // initialize empty container
    }

    // --- Run Excel-based template dynamically ---
    public void runTemplateExample(String excelTemplatePath, String drtTemplatePath) throws Exception {
        InputStream templateStream = getClass().getResourceAsStream(drtTemplatePath);
        InputStream excelStream = getClass().getResourceAsStream(excelTemplatePath);

        List<Map<String, Object>> data = readExcelToData(excelStream);

        ObjectDataCompiler compiler = new ObjectDataCompiler();
        String drl = compiler.compile(data, templateStream);
        System.out.println("✅ Generated DRL from Excel Template:\n" + drl);

        // Add to in-memory KieFileSystem and rebuild
        kieFileSystem.write("src/main/resources/rules/generated/dynamic-template.drl", drl);
        rebuildContainer();

        // Run a sample session
        KieSession ksession = kieContainer.newKieSession();
        Machine m1 = new Machine("M1", "Pump A");
        m1.setTemperature(95.0);
        ksession.insert(m1);

        ksession.fireAllRules();
        ksession.dispose();
    }

    // --- Add a raw DRL rule at runtime ---
    public void addRule(String ruleName, String drl) {
        kieFileSystem.write("src/main/resources/rules/generated/" + ruleName + ".drl", drl);
        rebuildContainer();
    }

    // --- Read Excel to List<Map<String,Object>> ---
    private List<Map<String, Object>> readExcelToData(InputStream excelStream) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(excelStream);
        Sheet sheet = workbook.getSheetAt(0);

        // Read header row
        Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) headers.add(cell.getStringCellValue());

        // Read data rows
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Map<String, Object> rowData = new HashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j);
                Object value;
                switch (cell.getCellType()) {
                    case STRING:
                        value = cell.getStringCellValue();
                        break;
                    case NUMERIC:
                        value = cell.getNumericCellValue();
                        break;
                    case BOOLEAN:
                        value = cell.getBooleanCellValue();
                        break;
                    default:
                        value = null;
                        break;
                }
                rowData.put(headers.get(j), value);
            }
            data.add(rowData);
        }

        workbook.close();
        return data;
    }

    // --- Rebuild container safely ---
    private void rebuildContainer() {
        KieBuilder builder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        if (builder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Error building KieBase: " + builder.getResults());
        }
        this.kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
    }
}
