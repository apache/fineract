Feature: Service Modules

  @modules
  Scenario Outline: Verify that the dummy service returns the correct message
    Given An auto configuration <autoConfigurationClass> and a service configuration <configurationClass>
    When The user retrieves the service of interface class <interfaceClass>
    Then The service class should match <serviceClass>

    Examples:
      | autoConfigurationClass                                           | configurationClass                                           | interfaceClass                                                      | serviceClass                                                                         |
      | org.apache.fineract.portfolio.note.starter.NoteAutoConfiguration | org.apache.fineract.module.service.TestDefaultConfiguration  | org.apache.fineract.portfolio.note.service.NoteReadPlatformService  | org.apache.fineract.portfolio.note.service.NoteReadPlatformServiceImpl               |
      | org.apache.fineract.portfolio.note.starter.NoteAutoConfiguration | org.apache.fineract.module.service.TestDefaultConfiguration  | org.apache.fineract.portfolio.note.service.NoteWritePlatformService | org.apache.fineract.portfolio.note.service.NoteWritePlatformServiceJpaRepositoryImpl |
      | org.apache.fineract.portfolio.note.starter.NoteAutoConfiguration | org.apache.fineract.module.service.TestOverrideConfiguration | org.apache.fineract.portfolio.note.service.NoteReadPlatformService  | org.apache.fineract.module.service.custom.CustomNoteReadPlatformService              |
      | org.apache.fineract.portfolio.note.starter.NoteAutoConfiguration | org.apache.fineract.module.service.TestOverrideConfiguration | org.apache.fineract.portfolio.note.service.NoteWritePlatformService | org.apache.fineract.module.service.custom.CustomNoteWritePlatformService             |