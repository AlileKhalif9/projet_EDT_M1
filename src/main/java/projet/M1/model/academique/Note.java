package projet.M1.model.academique;

public class Note {
    // Vérifier que ça soit bien unsigned float
    private float valeur;
    private float coefficient;

    private Module module;

    public Note(float valeur, float coefficient, Module module) {
        this.valeur = valeur;
        this.coefficient = coefficient;
        this.module = module;
    }

    public float getValeur() {
        return valeur;
    }

    public void setValeur(float valeur) {
        this.valeur = valeur;
    }

    public float getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(float coefficient) {
        this.coefficient = coefficient;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }
}
