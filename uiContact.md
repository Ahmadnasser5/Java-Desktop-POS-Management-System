# UI Contract — Java Desktop Cashier System

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
Layout ResponsibilitiesTop Bar: Displays branding, user information, and authentication controls.Sidebar: Handles primary navigation.Content Area: Reserved exclusively for feature implementation.Status Bar: Displays application state and versioning.Note: Feature developers must work exclusively inside the Content area.3. Window Dimensions & Layout ManagersThe application is designed desktop-first with fixed bounds restriction.Default Resolution: 1280px × 720pxMinimum Resolution: 1024px × 650pxLayout Manager PolicyAbsolute positioning (e.g., setBounds(...)) is forbidden except in strictly justified edge cases. Approved Swing/AWT layout managers include:BorderLayoutGridLayoutGridBagLayoutBoxLayoutFlowLayoutCardLayout4. Color PaletteNameHex CodeUsage / PurposePrimary#2563EBPrimary actions, main buttonsPrimary Dark#1D4ED8Active or hover states for primary actionsBackground#F8FAFCApplication window backgroundSurface#FFFFFFCards, forms, modals, tablesText Primary#0F172AHeadings and primary textText Secondary#64748BLabels, captions, muted informationBorder#E2E8F0Component borders, table grid linesSuccess#16A34AConfirmations, success badges, positive actionsWarning#F59E0BSystem alerts, warning badgesDanger#DC2626Destructive actions, errors, delete buttonsSidebar#0F172ANavigation bar background5. TypographyA single font family, Segoe UI, is used application-wide.ElementStyleSizeApp TitleBold24pxPage TitleBold22pxSection TitleBold18pxNormal TextPlain14pxSecondary TextPlain13pxButton TextBold14pxTable TextPlain13px6. Spacing SystemAll margins, paddings, and gaps must strictly use multiples of 4px.Page Padding: 24pxCard Padding: 16pxComponent Gap: 12pxSection Gap: 24px7. Component SpecificationsButtonsAll buttons must feature a fixed height of 40px, horizontal padding of 16px, border-radius of 6px, and 14px Bold typography.Primary (#2563EB): Main actions (e.g., [ + Add Product ])Secondary (#E2E8F0): Neutral actions (e.g., [ Cancel ])Danger (#DC2626): Destructive actions (e.g., [ Delete ])Success (#16A34A): Transaction completion (e.g., [ Complete Sale ])Form InputsHeight: 40pxBorder: 1px solid #E2E8F0Border Radius: 6pxPadding: 12pxStructure: Vertical alignment with the label positioned directly above the input field.Data TablesHeader: Bold text, 40px height, background #F8FAFCRow Height: 36px–40pxActions: Edit, Delete, or View options aligned cleanly in the final column.8. Package Architecture & Shared ComponentsDevelopers must use the shared UI library instead of instantiating raw Swing components directly.Plaintextsrc/
└── ui/
    ├── components/
    │   ├── AppButton.java
    │   ├── AppTextField.java
    │   ├── AppTable.java
    │   ├── AppCard.java
    │   ├── ConfirmDialog.java
    │   ├── Toast.java
    │   └── LoadingPanel.java
    │
    ├── layout/
    │   ├── MainFrame.java
    │   ├── Sidebar.java
    │   ├── TopBar.java
    │   └── StatusBar.java
    │
    └── theme/
        ├── AppColors.java
        ├── AppFonts.java
        └── AppTheme.java
Git Governance Rule: Changes to ui/components/, ui/theme/, or ui/layout/ require prior team approval before being merged.9. Naming ConventionsVariable names must be descriptive and explicit. Generic or numbered component names are prohibited.❌ JButton button1;❌ JTextField textField2;✅ JButton addProductButton;✅ JTextField productNameField;✅ JPanel productsPanel;10. Definition of Done (UI Verification Checklist)A feature is considered complete only when all the following criteria are met:[ ] Color, font, and spacing implementations match the design system.[ ] Components are instantiated strictly via ui.components.[ ] Interface resizes smoothly down to the minimum resolution (1024px × 650px).[ ] Empty, Loading, and Error states are implemented.[ ] User permissions correctly toggle menu items and UI actions.[ ] Naming conventions are applied to all UI fields.
