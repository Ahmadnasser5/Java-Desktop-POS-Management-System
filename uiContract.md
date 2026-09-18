# UI Contract — JavaFX Desktop Cashier System

## 1. Objective
All screens within the project must strictly adhere to this uniform design system. Individual component styling or custom UI implementation outside these defined standards is strictly prohibited to ensure visual consistency across the entire application.

---

## 2. Application Layout
All post-authentication screens must use the standardized application frame:

```text
┌──────────────────────────────────────────────────────────────┐
│                        TOP BAR                               │
│  Logo / System Name                         User | Logout    │
├──────────────┬───────────────────────────────────────────────┤
│              │                                               │
│   SIDEBAR    │                  CONTENT                      │
│              │                                               │
│ Dashboard    │                                               │
│ Sales        │                                               │
│ Products     │                                               │
│ Categories   │                                               │
│ Users        │                                               │
│ Reports      │                                               │
│              │                                               │
├──────────────┴───────────────────────────────────────────────┤
│                        STATUS BAR                            │
└──────────────────────────────────────────────────────────────┘
Layout ResponsibilitiesTop Bar: Displays branding, user information, and authentication controls.Sidebar: Handles primary navigation.Content Area: Reserved exclusively for feature implementation.Status Bar: Displays application state and versioning.Note: Feature developers must work exclusively inside the Content area (StackPane or AnchorPane inside BorderPane.center).3. Window Dimensions & Layout PanesThe application is designed desktop-first with responsive layout containers.Default Resolution: 1280px × 720pxMinimum Resolution: 1024px × 650pxLayout PolicyAbsolute positioning (e.g., setLayoutX() / setLayoutY()) is strictly prohibited. Approved JavaFX layout containers include:BorderPaneGridPaneVBoxHBoxStackPaneFlowPaneScrollPane4. Color Palette & CSS VariablesAll colors must be defined as CSS variables in style.css and referenced globally.CSS VariableHex CodeUsage / Purpose-fx-primary#2563EBPrimary actions, main buttons-fx-primary-dark#1D4ED8Active or hover states for primary actions-fx-background#F8FAFCMain window background-fx-surface#FFFFFFCards, forms, modals, tables-fx-text-primary#0F172AHeadings and primary text-fx-text-secondary#64748BLabels, captions, muted information-fx-border#E2E8F0Component borders, table grid lines-fx-success#16A34AConfirmations, success badges, positive actions-fx-warning#F59E0BSystem alerts, warning badges-fx-danger#DC2626Destructive actions, errors, delete buttons-fx-sidebar#0F172ANavigation bar background5. TypographyA single font family, Segoe UI, is used application-wide.ElementStyleSizeApp TitleBold24pxPage TitleBold22pxSection TitleBold18pxNormal TextRegular14pxSecondary TextRegular13pxButton TextBold14pxTable TextRegular13px6. Spacing SystemAll margins, paddings, and gaps must strictly use multiples of 4px.Page Padding: 24pxCard Padding: 16pxComponent Gap (vgap / hgap / spacing): 12pxSection Gap: 24px7. Component SpecificationsButtonsAll Button components must feature a fixed height of 40px, horizontal padding of 16px, border-radius of 6px, and 14px Bold typography.Primary (.btn-primary): Main actions (e.g., [ + Add Product ])Secondary (.btn-secondary): Neutral actions (e.g., [ Cancel ])Danger (.btn-danger): Destructive actions (e.g., [ Delete ])Success (.btn-success): Transaction completion (e.g., [ Complete Sale ])Form InputsTextField and PasswordField specifications:Height: 40px (-fx-pref-height: 40px;)Border: 1px solid #E2E8F0Border Radius: 6pxPadding: 12pxStructure: Vertical alignment (VBox) with the Label positioned directly above the input field.Data Tables (TableView)Header: Bold text, 40px height, background #F8FAFCRow Height: 36px–40px (-fx-fixed-cell-size: 38px;)Actions: Edit, Delete, or View options aligned cleanly in a dedicated TableColumn using TableCell actions.8. Package Architecture & Shared ComponentsDevelopers must use custom controls and FXML components instead of instantiating raw controls manually.Plaintextsrc/main/
├── java/
│   └── ui/
│       ├── components/
│       │   ├── AppButton.java
│       │   ├── AppTextField.java
│       │   ├── AppTableView.java
│       │   ├── AppCard.java
│       │   ├── ConfirmDialog.java
│       │   ├── Toast.java
│       │   └── LoadingOverlay.java
│       │
│       ├── layout/
│       │   ├── MainLayoutController.java
│       │   ├── SidebarController.java
│       │   ├── TopBarController.java
│       │   └── StatusBarController.java
│       │
│       └── theme/
│           └── ThemeManager.java
│
└── resources/
    └── css/
        ├── style.css
        └── variables.css
Git Governance Rule: Changes to ui/components/, resources/css/, or ui/layout/ require prior team approval before being merged.9. Naming Conventionsfx:id and controller fields must be descriptive and explicit. Generic or numbered component names are prohibited.❌ @FXML private Button button1;❌ @FXML private TextField textField2;✅ @FXML private Button addProductButton;✅ @FXML private TextField productNameField;✅ @FXML private VBox productsContainerPanel;10. Definition of Done (UI Verification Checklist)A feature is considered complete only when all the following criteria are met:[ ] CSS classes from style.css are used for all styles (no inline -fx-style overrides).[ ] Interface layout scales smoothly down to the minimum resolution (1024px × 650px).[ ] No setLayoutX() / setLayoutY() calls are present in Java code or FXML.[ ] Empty, Loading, and Error states are implemented using LoadingOverlay or PlaceholderNode.[ ] User permissions correctly hide or disable restricted navigation nodes.[ ] All fx:id declarations follow standard JavaFX naming conventions.
