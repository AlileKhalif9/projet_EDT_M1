package projet.M1.model.utilisateur_systeme;

public sealed class Utilisateur
        permits Etudiant, Professeur, Gestionnaire_Planning, Invite {

    private String nom;
    private String prenom;
    private int age;
    private String login;
    private String motDePasse;

    public Utilisateur(String nom, String prenom, int age, String login, String motDePasse) {
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.login = login;
        this.motDePasse = motDePasse;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}