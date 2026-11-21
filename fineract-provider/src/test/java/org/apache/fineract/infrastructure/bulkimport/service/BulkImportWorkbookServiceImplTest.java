/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.bulkimport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants;
import org.apache.fineract.infrastructure.bulkimport.data.BulkImportEvent;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocument;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.mapping.ImportDocumentMapper;
import org.apache.fineract.infrastructure.bulkimport.populator.CenterSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ChargeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ClientSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ExtrasSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.FixedDepositProductSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.GlAccountSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.GroupSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.LoanProductSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.RecurringDepositProductSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.RoleSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.SavingsAccountSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.SavingsProductSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.SharedProductsSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.WorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.centers.CentersWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.chartofaccounts.ChartOfAccountsWorkbook;
import org.apache.fineract.infrastructure.bulkimport.populator.client.ClientEntityWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.client.ClientPersonWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.fixeddeposits.FixedDepositTransactionWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.fixeddeposits.FixedDepositWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.group.GroupsWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.guarantor.GuarantorWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.journalentry.JournalEntriesWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.loan.LoanWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.loanrepayment.LoanRepaymentWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.office.OfficeWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.recurringdeposit.RecurringDepositTransactionWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.recurringdeposit.RecurringDepositWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.savings.SavingsTransactionsWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.savings.SavingsWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.shareaccount.SharedAccountWorkBookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.staff.StaffWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.users.UserWorkbookPopulator;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BulkImportWorkbookServiceImplTest {

    private static final String OFFICES = GlobalEntityType.OFFICES.toString();
    private static final String LOCALE = "en";
    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final OfficeData OFFICE = new OfficeData(1L, "Head Office", "Head Office", null, LocalDate.of(2020, 1, 1), ".", null,
            null, null);

    @Mock
    private TransactionBoundApplicationEventPublisher eventPublisher;
    @Mock
    private PlatformSecurityContext securityContext;
    @Mock
    private ImportDocumentRepository importDocumentRepository;
    @Mock
    private DocumentWritePlatformService writePlatformService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ContentPipe pipe;
    @Mock
    private ImportDocumentMapper mapper;
    @Mock
    private AppUser user;

    private BulkImportWorkbookServiceImpl service;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 11, 6))));
        service = new BulkImportWorkbookServiceImpl(eventPublisher, securityContext, importDocumentRepository, writePlatformService,
                jdbcTemplate, pipe, mapper);
        when(securityContext.authenticatedUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(writePlatformService.createDocument(any())).thenReturn(DocumentCreateResponse.builder().resourceId(10L).build());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = { "offices.xls", "OFFICES.XLS", "Offices.Xls" })
    void acceptsXlsInAnyCase(final String fileName) throws IOException {
        upload(fileName, workbookBytes(new HSSFWorkbook(), "Offices"));

        verify(writePlatformService).createDocument(any());
        verify(eventPublisher).publishEvent(any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "offices.xlsx", "OFFICES.XLSX" })
    void acceptsXlsxInAnyCase(final String fileName) throws IOException {
        upload(fileName, workbookBytes(new XSSFWorkbook(), "Offices"));

        verify(writePlatformService).createDocument(any());
    }

    @Test
    void rejectsFileWithoutExtension() throws IOException {
        assertRejected(upload("offices_template", workbookBytes(new HSSFWorkbook(), "Offices")),
                "error.msg.bulk.import.file.extension.missing", "File has no extension - only .xls and .xlsx are accepted");
    }

    @Test
    void rejectsUnsupportedExtension() throws IOException {
        assertRejected(upload("offices.csv", workbookBytes(new HSSFWorkbook(), "Offices")), "error.msg.bulk.import.file.extension.invalid",
                "File extension .csv is not supported - only .xls and .xlsx are accepted");
    }

    @Test
    void rejectsFileNameLongerThanTheDocumentColumn() throws IOException {
        final String fileName = "x".repeat(BulkImportWorkbookServiceImpl.MAX_FILE_NAME_LENGTH - 3) + ".xls";

        assertRejected(upload(fileName, workbookBytes(new HSSFWorkbook(), "Offices")), "error.msg.bulk.import.file.name.too.long",
                "File name is too long (max 250 characters)");
    }

    @Test
    void acceptsFileNameAtTheDocumentColumnLimit() throws IOException {
        final String fileName = "x".repeat(BulkImportWorkbookServiceImpl.MAX_FILE_NAME_LENGTH - 4) + ".xls";

        upload(fileName, workbookBytes(new HSSFWorkbook(), "Offices"));

        verify(writePlatformService).createDocument(any());
    }

    @Test
    void rejectsWorkbookWithoutTheEntitySheet() throws IOException {
        assertRejected(upload("blank_workbook.xlsx", workbookBytes(new XSSFWorkbook(), "Sheet1")),
                "error.msg.bulk.import.file.not.expected.template",
                "This file is not an Offices template (sheet 'Offices' with the Offices columns not found)");
    }

    @Test
    void rejectsContentThatIsNotAWorkbook() {
        assertRejected(upload("offices.xls", "Office Name,Parent Office\nA,Head Office\n".getBytes(StandardCharsets.UTF_8)),
                "error.msg.bulk.import.file.unreadable", "File could not be read as an Excel workbook");
    }

    @Test
    void rejectsCorruptWorkbook() throws IOException {
        final byte[] valid = workbookBytes(new XSSFWorkbook(), "Offices");
        final byte[] truncated = Arrays.copyOf(valid, valid.length / 3);

        assertRejected(upload("offices.xlsx", truncated), "error.msg.bulk.import.file.unreadable",
                "File could not be read as an Excel workbook");
    }

    @ParameterizedTest
    @EnumSource(value = GlobalEntityType.class, names = { "OFFICES", "CENTERS", "CHART_OF_ACCOUNTS", "CLIENTS_ENTITY", "CLIENTS_PERSON",
            "FIXED_DEPOSIT_ACCOUNTS", "FIXED_DEPOSIT_TRANSACTIONS", "GROUPS", "GUARANTORS", "GL_JOURNAL_ENTRIES", "LOANS",
            "LOAN_TRANSACTIONS", "RECURRING_DEPOSIT_ACCOUNTS", "RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS", "SAVINGS_ACCOUNT",
            "SAVINGS_TRANSACTIONS", "SHARE_ACCOUNTS", "STAFF", "USERS" })
    void everyImportableEntityHasATemplateLayout(final GlobalEntityType entityType) {
        assertThat(BulkImportTemplateLayout.of(entityType)).isNotNull();
        assertThat(BulkImportTemplateLayout.of(entityType).sheetName()).isNotBlank();
    }

    private PlatformApiDataValidationException upload(final String fileName, final byte[] content) {
        final FormDataContentDisposition fileDetail = FormDataContentDisposition.name("file").fileName(fileName).size(content.length)
                .build();
        try {
            service.importWorkbook(OFFICES, new ByteArrayInputStream(content), fileDetail, LOCALE, DATE_FORMAT);
            return null;
        } catch (PlatformApiDataValidationException e) {
            return e;
        }
    }

    private void assertRejected(final PlatformApiDataValidationException exception, final String code, final String message) {
        assertThat(exception).isNotNull();
        assertThat(exception.getErrors()).hasSize(1);
        final ApiParameterError error = exception.getErrors().get(0);
        assertThat(error.getUserMessageGlobalisationCode()).isEqualTo(code);
        assertThat(error.getDefaultUserMessage()).isEqualTo(message);
        assertThat(error.getParameterName()).isEqualTo("file");
        verify(writePlatformService, never()).createDocument(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private static byte[] workbookBytes(final Workbook workbook, final String sheetName) throws IOException {
        try (workbook) {
            final Sheet sheet = workbook.createSheet(sheetName);
            sheet.createRow(0).createCell(0).setCellValue("Office Name");
            sheet.createRow(1).createCell(0).setCellValue("Branch");
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void realClientsPersonTemplateOnOfficesScreenIsRejected() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        new ClientPersonWorkbookPopulator(new OfficeSheetPopulator(List.of(OFFICE)),
                new PersonnelSheetPopulator(new ArrayList<>(), List.of(OFFICE)), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>()).populate(workbook, DATE_FORMAT);
        dataRows(workbook.getSheet(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME), 0, 2);
        assertThat(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME)).isNotNull();

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> uploadWorkbook(GlobalEntityType.OFFICES, workbook));

        assertThat(exception.getErrors()).singleElement().satisfies(error -> {
            assertThat(error.getUserMessageGlobalisationCode()).isEqualTo("error.msg.bulk.import.file.not.expected.template");
            assertThat(error.getDefaultUserMessage())
                    .isEqualTo("This file is not an Offices template (sheet 'Offices' with the Offices columns not found)");
        });
        assertNothingCreated();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("realTemplates")
    void everyRealTemplateIsAcceptedForItsOwnEntity(final GlobalEntityType entityType, final Supplier<WorkbookPopulator> populator)
            throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        populator.get().populate(workbook, DATE_FORMAT);
        final BulkImportTemplateLayout layout = BulkImportTemplateLayout.of(entityType);
        dataRows(workbook.getSheet(layout.sheetName()), layout.primaryColumn(), 2);

        assertThat(uploadedDocument(entityType, workbook).getTotalRecords()).isEqualTo(2);
    }

    private static Stream<Arguments> realTemplates() {
        final List<OfficeData> offices = List.of(OFFICE);
        final Supplier<OfficeSheetPopulator> officeSheet = () -> new OfficeSheetPopulator(offices);
        final Supplier<PersonnelSheetPopulator> staffSheet = () -> new PersonnelSheetPopulator(new ArrayList<>(), offices);
        final Supplier<ClientSheetPopulator> clientSheet = () -> new ClientSheetPopulator(new ArrayList<>(), offices);
        final Supplier<GroupSheetPopulator> groupSheet = () -> new GroupSheetPopulator(new ArrayList<>(), offices);
        final Supplier<ExtrasSheetPopulator> extrasSheet = () -> new ExtrasSheetPopulator(new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
        return Stream.of(
                template(GlobalEntityType.CLIENTS_PERSON,
                        () -> new ClientPersonWorkbookPopulator(officeSheet.get(), staffSheet.get(), new ArrayList<>(), new ArrayList<>(),
                                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())),
                template(GlobalEntityType.CLIENTS_ENTITY,
                        () -> new ClientEntityWorkbookPopulator(officeSheet.get(), staffSheet.get(), new ArrayList<>(), new ArrayList<>(),
                                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())),
                template(GlobalEntityType.CENTERS,
                        () -> new CentersWorkbookPopulator(officeSheet.get(), staffSheet.get(), groupSheet.get())),
                template(GlobalEntityType.GROUPS,
                        () -> new GroupsWorkbookPopulator(officeSheet.get(), staffSheet.get(),
                                new CenterSheetPopulator(new ArrayList<>(), offices), clientSheet.get())),
                template(GlobalEntityType.LOANS,
                        () -> new LoanWorkbookPopulator(officeSheet.get(), clientSheet.get(), groupSheet.get(), staffSheet.get(),
                                new LoanProductSheetPopulator(new ArrayList<>()), new ChargeSheetPopulator(new ArrayList<>()),
                                extrasSheet.get())),
                template(GlobalEntityType.LOAN_TRANSACTIONS,
                        () -> LoanRepaymentWorkbookPopulator.full(new ArrayList<>(), officeSheet.get(), clientSheet.get(),
                                extrasSheet.get())),
                template(GlobalEntityType.GUARANTORS,
                        () -> new GuarantorWorkbookPopulator(officeSheet.get(), clientSheet.get(), new ArrayList<>(), new ArrayList<>(),
                                new ArrayList<>())),
                template(GlobalEntityType.OFFICES, () -> new OfficeWorkbookPopulator(offices)),
                template(GlobalEntityType.CHART_OF_ACCOUNTS,
                        () -> new ChartOfAccountsWorkbook(new ArrayList<>(), offices, new ArrayList<>())),
                template(GlobalEntityType.GL_JOURNAL_ENTRIES,
                        () -> new JournalEntriesWorkbookPopulator(officeSheet.get(), new GlAccountSheetPopulator(new ArrayList<>()),
                                extrasSheet.get())),
                template(GlobalEntityType.STAFF, () -> new StaffWorkbookPopulator(officeSheet.get())),
                template(GlobalEntityType.SHARE_ACCOUNTS,
                        () -> new SharedAccountWorkBookPopulator(new SharedProductsSheetPopulator(new ArrayList<>(), new ArrayList<>()),
                                clientSheet.get(), new SavingsAccountSheetPopulator(new ArrayList<>()))),
                template(GlobalEntityType.SAVINGS_ACCOUNT,
                        () -> new SavingsWorkbookPopulator(officeSheet.get(), clientSheet.get(), groupSheet.get(), staffSheet.get(),
                                new SavingsProductSheetPopulator(new ArrayList<>()))),
                template(GlobalEntityType.SAVINGS_TRANSACTIONS,
                        () -> new SavingsTransactionsWorkbookPopulator(officeSheet.get(), clientSheet.get(), extrasSheet.get(),
                                new ArrayList<>())),
                template(GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS,
                        () -> new RecurringDepositWorkbookPopulator(officeSheet.get(), clientSheet.get(), staffSheet.get(),
                                new RecurringDepositProductSheetPopulator(new ArrayList<>()))),
                template(GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS,
                        () -> new RecurringDepositTransactionWorkbookPopulator(officeSheet.get(), clientSheet.get(), extrasSheet.get(),
                                new ArrayList<>())),
                template(GlobalEntityType.FIXED_DEPOSIT_ACCOUNTS,
                        () -> new FixedDepositWorkbookPopulator(officeSheet.get(), clientSheet.get(), staffSheet.get(),
                                new FixedDepositProductSheetPopulator(new ArrayList<>()))),
                template(GlobalEntityType.FIXED_DEPOSIT_TRANSACTIONS,
                        () -> new FixedDepositTransactionWorkbookPopulator(officeSheet.get(), clientSheet.get(), extrasSheet.get(),
                                new ArrayList<>())),
                template(GlobalEntityType.USERS,
                        () -> new UserWorkbookPopulator(officeSheet.get(), staffSheet.get(), new RoleSheetPopulator(new ArrayList<>()))));
    }

    private static Arguments template(final GlobalEntityType entityType, final Supplier<WorkbookPopulator> populator) {
        return Arguments.of(entityType, populator);
    }

    @Test
    void clientsTemplateOnOfficesScreenIsRejectedWithoutCreatingAnImport() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        sheet(workbook, TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME, 0, "First Name*");
        final Sheet officesLookup = sheet(workbook, TemplatePopulateImportConstants.OFFICE_SHEET_NAME, 0, "ID");
        officesLookup.getRow(0).createCell(1).setCellValue("Name");
        dataRows(officesLookup, 0, 3);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> uploadWorkbook(GlobalEntityType.OFFICES, workbook));

        final String message = "This file is not an Offices template (sheet 'Offices' with the Offices columns not found)";
        assertThat(exception.getDefaultUserMessage()).isEqualTo(message);
        assertThat(exception.getErrors()).singleElement().satisfies(error -> {
            assertThat(error.getParameterName()).isEqualTo("file");
            assertThat(error.getDefaultUserMessage()).isEqualTo(message);
            assertThat(error.getDeveloperMessage()).isEqualTo(message);
        });
        assertNothingCreated();
    }

    @Test
    void fileWithoutTheTemplateSheetIsRejected() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        sheet(workbook, "Sheet1", 0, "Office Name*");

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> uploadWorkbook(GlobalEntityType.LOANS, workbook));

        assertThat(exception.getDefaultUserMessage())
                .isEqualTo("This file is not a Loans template (sheet 'Loans' with the Loans columns not found)");
        assertNothingCreated();
    }

    @Test
    void emptySheetIsRejected() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);

        assertThrows(PlatformApiDataValidationException.class, () -> uploadWorkbook(GlobalEntityType.OFFICES, workbook));
        assertNothingCreated();
    }

    @Test
    void realOfficesTemplateIsAccepted() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        new OfficeWorkbookPopulator(List.of()).populate(workbook, DATE_FORMAT);
        dataRows(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), 0, 2);

        assertThat(uploadedDocument(GlobalEntityType.OFFICES, workbook).getTotalRecords()).isEqualTo(2);
    }

    @Test
    void headerMatchIgnoresMandatoryMarkerSpacingAndCase() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        dataRows(sheet(workbook, TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME, 0, " office  name * "), 0, 1);

        assertThat(uploadedDocument(GlobalEntityType.STAFF, workbook).getTotalRecords()).isEqualTo(1);
    }

    @Test
    void totalIsCountedOnTheSheetTheHandlerReadsNotTheFirstSheet() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        dataRows(sheet(workbook, "Instructions", 0, "Notes"), 0, 8);
        dataRows(sheet(workbook, TemplatePopulateImportConstants.OFFICE_SHEET_NAME, 0, "Office Name*"), 0, 3);

        assertThat(uploadedDocument(GlobalEntityType.OFFICES, workbook).getTotalRecords()).isEqualTo(3);
    }

    @Test
    void savingsTransactionsTotalIsCountedOnTheAmountColumnLikeTheHandler() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        final Sheet sheet = sheet(workbook, TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME, TransactionConstants.AMOUNT_COL,
                "Amount*");
        dataRows(sheet, TransactionConstants.AMOUNT_COL, 17);
        // an office left blank on a row used to end the count there, although the handler still processes it
        for (int rowIndex = 1; rowIndex <= 15; rowIndex++) {
            sheet.getRow(rowIndex).createCell(TransactionConstants.OFFICE_NAME_COL).setCellValue("Head Office");
        }

        assertThat(uploadedDocument(GlobalEntityType.SAVINGS_TRANSACTIONS, workbook).getTotalRecords()).isEqualTo(17);
    }

    @Test
    void unknownEntityIsStillRefused() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        sheet(workbook, TemplatePopulateImportConstants.OFFICE_SHEET_NAME, 0, "Office Name*");

        assertThrows(GeneralPlatformDomainRuleException.class, () -> uploadWorkbook(GlobalEntityType.SMS, workbook));
        assertNothingCreated();
    }

    private ImportDocument uploadedDocument(final GlobalEntityType entityType, final Workbook workbook) throws IOException {
        uploadWorkbook(entityType, workbook);
        final ArgumentCaptor<BulkImportEvent> event = ArgumentCaptor.forClass(BulkImportEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        return event.getValue().getImportDocument();
    }

    private void uploadWorkbook(final GlobalEntityType entityType, final Workbook workbook) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        workbook.write(bytes);
        service.importWorkbook(entityType.toString(), new ByteArrayInputStream(bytes.toByteArray()),
                FormDataContentDisposition.name("file").fileName("upload.xls").size(bytes.size()).build(), LOCALE, DATE_FORMAT);
    }

    private void assertNothingCreated() {
        verify(writePlatformService, never()).createDocument(any());
        verify(importDocumentRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private Sheet sheet(final Workbook workbook, final String name, final int column, final String header) {
        final Sheet sheet = workbook.createSheet(name);
        sheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX).createCell(column).setCellValue(header);
        return sheet;
    }

    private void dataRows(final Sheet sheet, final int column, final int count) {
        for (int rowIndex = 1; rowIndex <= count; rowIndex++) {
            final Row row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
            row.createCell(column).setCellValue("value " + rowIndex);
        }
    }
}
