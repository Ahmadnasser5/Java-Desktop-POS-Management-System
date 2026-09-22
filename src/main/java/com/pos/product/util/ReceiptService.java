package com.pos.product.util;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.pos.sales.PosController;

import javafx.collections.ObservableList;

public class ReceiptService implements Printable {

    private static final double RECEIPT_WIDTH = 226.77;
    private static final double RECEIPT_HEIGHT = 800;
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 9);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 12);

    private final ObservableList<PosController.CartLine> cartLines;
    private final String receiptNo;
    private final String cashierName;
    private final double discountAmount;
    private final double taxAmount;

    public ReceiptService(ObservableList<PosController.CartLine> cartLines, String receiptNo,
                          String cashierName, double discountAmount, double taxAmount) {
        if (cartLines == null) {
            throw new IllegalArgumentException("cartLines must not be null");
        }
        this.cartLines = cartLines;
        this.receiptNo = receiptNo == null ? "" : receiptNo;
        this.cashierName = cashierName == null ? "" : cashierName;
        this.discountAmount = Math.max(0, discountAmount);
        this.taxAmount = Math.max(0, taxAmount);
    }

    /** Opens the printer dialog and sends the receipt to the selected printer. */
    public boolean print() {
        if (cartLines.isEmpty()) {
            return false;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pageFormat = job.defaultPage();
        Paper paper = new Paper();
        paper.setSize(RECEIPT_WIDTH, RECEIPT_HEIGHT);
        paper.setImageableArea(8, 8, RECEIPT_WIDTH - 16, RECEIPT_HEIGHT - 16);
        pageFormat.setPaper(paper);
        job.setPrintable(this, pageFormat);

        try {
            return job.printDialog() && printJob(job);
        } catch (PrinterException | RuntimeException exception) {
            return false;
        }
    }

    private boolean printJob(PrinterJob job) throws PrinterException {
        job.print();
        return true;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        graphics2D.setFont(BODY_FONT);
        FontMetrics metrics = graphics2D.getFontMetrics();
        int y = metrics.getAscent();

        graphics2D.setFont(HEADER_FONT);
        drawCentered(graphics2D, "فاتورة مبيعات", RECEIPT_WIDTH / 2, y);
        graphics2D.setFont(BODY_FONT);
        y += metrics.getHeight() + 4;
        y = drawLine(graphics2D, "رقم الفاتورة: " + receiptNo, y);
        y = drawLine(graphics2D, "التاريخ: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(new Date()), y);
        y = drawLine(graphics2D, "الكاشير: " + cashierName, y);
        y += 4;
        y = drawSeparator(graphics2D, y);

        double subtotal = 0;
        for (PosController.CartLine line : cartLines) {
            subtotal += line.lineTotal();
            String description = line.product().name() + " x" + line.quantity();
            y = drawLine(graphics2D, description, y);
            y = drawLine(graphics2D, money(line.lineTotal()), y);
        }

        double total = Math.max(0, subtotal + taxAmount - discountAmount);
        y += 4;
        y = drawSeparator(graphics2D, y);
        y = drawAmount(graphics2D, "المجموع", subtotal, y);
        y = drawAmount(graphics2D, "الضريبة", taxAmount, y);
        y = drawAmount(graphics2D, "الخصم", discountAmount, y);
        y = drawAmount(graphics2D, "الإجمالي", total, y);
        y = drawAmount(graphics2D, "الدفع نقداً", total, y);
        drawCentered(graphics2D, "شكراً لزيارتكم", RECEIPT_WIDTH / 2, y + 14);
        return PAGE_EXISTS;
    }

    private int drawLine(Graphics2D graphics, String text, int y) {
        graphics.drawString(text, 0, y);
        return y + graphics.getFontMetrics().getHeight();
    }

    private int drawAmount(Graphics2D graphics, String label, double amount, int y) {
        String value = money(amount);
        graphics.drawString(label, 0, y);
        int valueWidth = graphics.getFontMetrics().stringWidth(value);
        graphics.drawString(value, (int) RECEIPT_WIDTH - valueWidth, y);
        return y + graphics.getFontMetrics().getHeight();
    }

    private int drawSeparator(Graphics2D graphics, int y) {
        graphics.drawString("--------------------------------", 0, y);
        return y + graphics.getFontMetrics().getHeight();
    }

    private void drawCentered(Graphics2D graphics, String text, double centerX, int y) {
        int textWidth = graphics.getFontMetrics().stringWidth(text);
        graphics.drawString(text, (int) centerX - textWidth / 2, y);
    }

    private String money(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
    }
}
