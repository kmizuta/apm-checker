# ApmChecker

ApmChecker will analyze the application.json from GIT and get the full list
of App Packages and their transitive closure of dependencies. 

## How to Run

Clone this repository and then build using Maven...

```shell
% mvn pacakge
```

To run, you simply need to run the generated JAR file...

```shell
% java -jar target/AppPackageChecker-1.0-SNAPSHOT.jar <config file>
```

## Config File format

The config file can be a properties file like the following:
```properties
application.repoUrl=ssh://kenichi.mizuta%40oracle.com@alm.oraclecorp.com/fusionapps_rwd-infrastructure-services_28459/apppkgdeploy-ms.git
application.repoPath=intg/helm/etc/config/application.json
```

| Property Name | Description                                                                                                                  |
|---------------|------------------------------------------------------------------------------------------------------------------------------|
| application.repoUrl | The SSH URL to the GIT repo. Currently does not support passing in credentials so the HTTP URLs won't work.                  |
| application.repoPath | This is the path in the GIT repo to the application.json file. If omitted, defaults to intg/helm/etc/config/application.json |

## Sample Output

```text
==============================================
Dependency Tree
==============================================
ora_erp_core_expensesSetup:2307.0.35 - This app package contains all Expense setup related Business Objects
   ora_erp_core_structure:2307.0.8 - This app package contains all core common structure Business Objects
      ora_hcm_hrCore_workstructures:2307.0.1 - App package containing business objects related to HCM workstructures functionality
      ora_erp_core_crmCommonSetup:2307.0.10 - CRM Common BOs
         ora_cxSales_common_partyModel:2210.0.40 - 
ora_erp_core_expenses:2307.0.35 - This app package contains all Expenses transaction related Business Objects
   ora_erp_core_commonObjects:2307.0.5 - This app package contains Business Objects used to retrieve Lookup types and values
   ora_erp_core_expensesSetup:2307.0.35 - This app package contains all Expense setup related Business Objects
      ora_erp_core_structure:2307.0.8 - This app package contains all core common structure Business Objects
         ora_hcm_hrCore_workstructures:2307.0.1 - App package containing business objects related to HCM workstructures functionality
         ora_erp_core_crmCommonSetup:2307.0.10 - CRM Common BOs
            ora_cxSales_common_partyModel:2210.0.40 - 
   ... Full depedency tree of App Packages ...


==============================================
List of App Packages
==============================================
ora_common_appsInfra_objects:2304.0.22
ora_cxSales_common_partyModel:2210.0.40
ora_cxSales_common_partyModel:2307.0.57
ora_cxSales_common_partyModel:2307.0.59
ora_erp_core_advancedCollections:2307.0.2
... Full list of App Packages in alphabetical order ....


```