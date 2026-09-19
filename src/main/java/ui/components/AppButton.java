package ui.components;

import javafx.scene.control.Button;

/**
 * SHARED COMPONENT - the only Button the project uses.
 * <p>
 * Sizing (40px height, 16px horizontal padding, 6px radius, 14px bold) comes
 * from style.css; this class only picks the variant class, so nobody has to
 * remember the class names.
 *
 * <pre>{@code <AppButton text="Save" variant="PRIMARY"/>}</pre>
 */
public class AppButton extends Button {

    public enum Variant {
        PRIMARY("btn-primary"),
        SECONDARY("btn-secondary"),
        DANGER("btn-danger"),
        SUCCESS("btn-success");

        private final String styleClass;

        Variant(String styleClass) {
            this.styleClass = styleClass;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }

    private Variant variant = Variant.PRIMARY;

    public AppButton() {
        applyVariant();
    }

    public AppButton(String text) {
        super(text);
        applyVariant();
    }

    public AppButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        applyVariant();
    }

    /** FXML-friendly setter: variant="DANGER" */
    public void setVariant(String variantName) {
        setVariant(Variant.valueOf(variantName.trim().toUpperCase()));
    }

    public void setVariant(Variant newVariant) {
        this.variant = newVariant;
        applyVariant();
    }

    public Variant getVariant() {
        return variant;
    }

    private void applyVariant() {
        for (Variant value : Variant.values()) {
            getStyleClass().remove(value.getStyleClass());
        }
        getStyleClass().add(variant.getStyleClass());
    }
}
