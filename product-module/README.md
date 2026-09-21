# Products & Inventory Module (Engineer 2)

جزء **Products & Inventory** مطابق للـ spec بالظبط. المسؤولية: Products, Categories, Stock, Low Stock.

## هيكل قاعدة البيانات (Table: products)

| العمود | النوع | الوصف |
|---|---|---|
| `id` | INT, AUTO_INCREMENT, PK | معرف فريد للمنتج |
| `name` | VARCHAR | اسم المنتج |
| `barcode` | VARCHAR, UNIQUE | باركود المنتج للبحث السريع / القراءة بالماسح |
| `price` | DECIMAL | سعر البيع |
| `stock_quantity` | INT | الكمية الحالية بالمخزن |
| `min_stock_level` | INT | الحد الأدنى قبل التنبيه |
| `category_id` | INT, FK → categories.id | القسم التابع له |

## هيكل الملفات

```
src/main/java/com/pos/product/
├── model/
│   ├── Product.java          # id, name, barcode, price, stockQuantity, minStockLevel, categoryId/categoryName
│   └── Category.java
├── dao/
│   ├── ProductDAO.java        # CRUD + بحث بالاسم/الباركود + reduceStock() + addStock()
│   └── CategoryDAO.java
├── controller/
│   └── ProductController.java # منطق شاشة الـ FXML + نافذة تقرير Low Stock
├── util/
│   └── DatabaseConnection.java
└── ProductModuleLauncher.java  # لتجربة الشاشة لوحدها فقط (اختياري)

src/main/resources/
├── fxml/ProductView.fxml
└── css/styles.css

sql/
└── products_schema.sql        # جداول categories و products + بيانات تجريبية
```

## خطوات الدمج في مشروعكم

1. **انسخ فولدر `com/pos/product`** بالكامل جوه `src/main/java` بتاع مشروعكم (عدّل الـ package name لو مشروعكم مبني على package تاني، وعدّل الـ imports في الملفات كلها بنفس الاسم).

2. **انسخ `fxml/ProductView.fxml` و `css/styles.css`** جوه `src/main/resources` بنفس البنية.

3. **شغّل السكريبت `products_schema.sql`** على الداتابيز المشترك بتاع الفريق:
   ```sql
   SOURCE sql/products_schema.sql;
   ```

4. **لو عندكم DB connection class مشترك خالص** (غالبًا هيكون عند Engineer 1 - Authentication)، امسح `DatabaseConnection.java` واستبدل الـ import في `ProductDAO.java` و`CategoryDAO.java` بالكلاس المشترك، بشرط يكون عنده method زي:
   ```java
   public static Connection getConnection() throws SQLException
   ```

5. **أضف MySQL connector للـ dependencies** لو مش موجود أصلًا (Maven):
   ```xml
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
       <version>8.3.0</version>
   </dependency>
   ```

6. **افتح الشاشة من الـ Main Menu / Sidebar بتاع المشروع** بدل `ProductModuleLauncher`:
   ```java
   Parent productView = FXMLLoader.load(getClass().getResource("/fxml/ProductView.fxml"));
   // احطها في الـ BorderPane/Tab بتاع الـ dashboard الأساسي
   ```
   بعد كده احذف `ProductModuleLauncher.java` لأنه كان بس لتجربة الشاشة لوحدها.

## المميزات المنفذة

- ✅ عرض/بحث/فلترة المنتجات (بالاسم أو الباركود)
- ✅ إضافة / تعديل / حذف منتج
- ✅ **إدارة كاملة للتصنيفات من داخل الواجهة**: زرار "إدارة التصنيفات" جنب حقل الفئة بيفتح شاشة منفصلة لإضافة تصنيفات جديدة وحذف تصنيفات موجودة (مع تأكيد قبل الحذف، وتنبيه إن المنتجات المرتبطة بالتصنيف المحذوف هتبقى بدون تصنيف مش هتتحذف)، وقائمة الفئات بتتحدّث تلقائيًا في الشاشة الرئيسية بعد الإغلاق
- ✅ تتبّع الكمية (`stock_quantity`)
- ✅ تنبيه Low Stock بثلاث طرق: (1) الصفوف اللي كميتها ≤ `min_stock_level` بتتلون تلقائيًا في الجدول، (2) عداد "N item(s) low on stock" أعلى الشاشة، (3) زرار "Low stock report" بيفتح نافذة منفصلة بقائمة كل المنتجات الناقصة

## نقطة التكامل مع Engineer 3 (POS & Sales)

بعد إتمام أي عملية بيع، Engineer 3 يستخدم الدالة دي في `ProductDAO` لخصم الكمية أوتوماتيك:

```java
ProductDAO productDAO = new ProductDAO();
boolean success = productDAO.reduceStock(productId, soldQuantity);
if (!success) {
    // معناها المخزون مش كافي - ارفض عملية البيع أو اعرض تنبيه
}
```

الدالة مبنية بشرط `stock_quantity >= soldQty` في نفس استعلام الـ `UPDATE`، فمفيش احتمال إن الكمية تنزل تحت الصفر حتى لو حصلت عمليتا بيع في نفس اللحظة (race condition). في اتجاه عكسي (توريد بضاعة جديدة)، فيه `addStock(productId, deliveredQty)` لإضافة كمية للمخزون.

كمان فيه `findByBarcode(String barcode)` لو شاشة الـ POS محتاجة تجيب بيانات المنتج مباشرة بعد قراءة الباركود بالماسح الضوئي.

## ملاحظات أمنية (مهمة)

- كل الاستعلامات مبنية بـ **PreparedStatement** (مفيش string concatenation)، يعني محمية من SQL Injection من الأساس
- عدّل بيانات الاتصال (`URL`, `USER`, `PASSWORD`) في `DatabaseConnection.java` قبل التشغيل - متسيبهاش فاضية أو `root` بلا باسورد في أي بيئة production
