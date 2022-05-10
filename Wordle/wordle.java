package wordle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Wordle extends Application {

    //p palabras, l letras, t teclas
    //maximo de palabras a pobrar 6, maximo de letras 5, total de teclas para pulsar 27
    private static int p = 6, l = 5, t = 27;

    private static int pJuego = 0;
    private static int lJuego = 0;

    private static Label letras[][]; //palabras jugadas
    private static Label teclas[]; //Teclas jugadas

    //Para conectarse a la base de datos
    private final String usuario = "wordle";
    private final String passwd = "wordle";
    //protocolo:mysql:localhost:puerto
    private final String cadConex = "jdbc:mysql://localhost:3306/";
    private final String basededatos = "wordle";

    private String palabra; //La palabra que se va a jugar
    private String palabraEnJuego = "";
    
    private GridPane teclado ;

    private int key[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public static void main(String[] args) {
        inicializar();
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        palabraAJugar();

        //importarInformacion();
        VBox vbox = new VBox();

        //Para darle estilos css
        vbox.getStylesheets().clear();
        vbox.getStylesheets().add("/estilos/estilos.css");

        GridPane tablero = new GridPane();
        tablero.setHgap(5);
        tablero.setVgap(5);
        tablero.setAlignment(Pos.CENTER);
        tablero.setPadding(new Insets(10));

        teclado = new GridPane();
        teclado.setHgap(5);
        teclado.setVgap(5);
        teclado.setAlignment(Pos.CENTER);
        teclado.setPadding(new Insets(10));

        for (int i = 0; i < p; i++) {
            for (int j = 0; j < l; j++) {
                letras[i][j] = new Label(" - ");
                letras[i][j].setMinSize(40, 40);
                letras[i][j].setAlignment(Pos.CENTER);
                tablero.add(letras[i][j], j, i);
            }
        }

        Scene escena = new Scene(vbox, 350, 450);

        teclas[0] = new Label("Q");
        teclas[1] = new Label("W");
        teclas[2] = new Label("E");
        teclas[3] = new Label("R");
        teclas[4] = new Label("T");
        teclas[5] = new Label("Y");
        teclas[6] = new Label("U");
        teclas[7] = new Label("I");
        teclas[8] = new Label("O");
        teclas[9] = new Label("P");

        teclas[10] = new Label("A");
        teclas[11] = new Label("S");
        teclas[12] = new Label("D");
        teclas[13] = new Label("F");
        teclas[14] = new Label("G");
        teclas[15] = new Label("H");
        teclas[16] = new Label("J");
        teclas[17] = new Label("K");
        teclas[18] = new Label("L");
        teclas[19] = new Label("Ñ");

        teclas[20] = new Label("Z");
        teclas[21] = new Label("X");
        teclas[22] = new Label("C");
        teclas[23] = new Label("V");
        teclas[24] = new Label("B");
        teclas[25] = new Label("N");
        teclas[26] = new Label("M");

        int cont = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                teclas[cont].setMinSize(30, 30);
                teclas[cont].setAlignment(Pos.CENTER);
                teclado.add(teclas[cont], j, i);
                cont++;
            }
        }

        stage.addEventHandler(KeyEvent.KEY_PRESSED, (evento) -> {
            if (evento.getCode() == KeyCode.ENTER && lJuego == 5) {
                resolverPalabra();
            } else if (evento.getCode() == KeyCode.BACK_SPACE) {
                borrarLetra();
            } else if (evento.getCode().isLetterKey()) {
                ponerLetra(evento.getCode().toString());
            } else if (evento.getText().equals("ñ")) {
                ponerLetra("Ñ");
            } else {
                System.out.println("Sin efecto");
            }
        });

        vbox.getChildren().addAll(tablero, teclado);

        stage.setScene(escena);
        stage.setTitle("Wordle");
        stage.show();
    }

    private static void inicializar() {
        letras = new Label[p][l];
        teclas = new Label[t];
    }

    private void resolverPalabra() {
        System.out.println("Resolver palabra");
        int cont = 0;
        for (int h = 0; h < 5; h++) {
            palabraEnJuego += letras[pJuego][h].getText();
        }
        System.out.println(palabraEnJuego);
        //Separamos nuestra palabra en letras para compararla con la que hemos metido nosotros 
        String letters[] = palabra.split("");
        if (existePalabra()) {
            for (int i = 0; i < 5; i++) {

                System.out.println("La letra no esta en la palabra");
                letras[pJuego][i].getStyleClass().clear();
                letras[pJuego][i].getStyleClass().add("Gris");

                for (int j = 0; j < 5; j++) {
                    //Comparamos letra a letra
                    if (letras[pJuego][i].getText().equalsIgnoreCase(letters[j])) {
                        //Comparamos si estan en la misma posicion
                        if (i == j) {
                            System.out.println("La posicion es igual");
                            letras[pJuego][i].getStyleClass().clear();
                            letras[pJuego][i].getStyleClass().add("Verde");
                            cont++;
                            System.out.println(cont);
                            for (int k = 0; k < 27; k++) {
                                if (teclas[k].getText().equals(letras[pJuego][i].getText())) {
                                    key[k] = 3;
                                }
                            }
                            break;
                            //y sino significa que 
                        } else {
                            System.out.println("La letra esta pero no es la posicion");
                            letras[pJuego][i].getStyleClass().clear();
                            letras[pJuego][i].getStyleClass().add("Amarillo");
                            for (int k = 0; k < 27; k++) {
                                if (teclas[k].getText().equals(letras[pJuego][i].getText())) {
                                    if(key[k]!=3){
                                        key[k] = 2;
                                    }
                                }
                            }

                        }
                    }
                }

                for (int k = 0; k < 27; k++) {
                    if (teclas[k].getText().equals(letras[pJuego][i].getText())) {
                        if (key[k] != 3) {
                            if (key[k] != 2) {
                                key[k] = 1;
                            }
                        }
                    }
                }

            }
            /*for (int i = 0; i < 5; i++) {
            System.out.println("La letra no esta en la palabra");
            letras[pJuego][i].getStyleClass().add("Gris");
        }  */
            if (pJuego < 7) {
                pJuego++;
                lJuego = 0;
                palabraEnJuego = "";
            }

            if (pJuego == 6) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("No pasa nada, de los errores se aprende");
                alert.setHeaderText("Has Perdido");
                alert.setContentText("¿Deseas volver a jugar");
                Optional<ButtonType> action = alert.showAndWait();
                for (int j = 0; j < 27; j++) {
                    key[j]=0;
                }
                if (action.get() == ButtonType.OK) {
                    reiniciarJuego();
                } else {
                    System.exit(0);
                }
            }
            if (cont == 5) {
                System.out.println("has ganado");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Enhorabuena listillo");
                alert.setHeaderText("Has Ganado");
                alert.setContentText("¿Deseas volver a jugar");
                Optional<ButtonType> action = alert.showAndWait();
                for (int j = 0; j < 27; j++) {
                    key[j]=0;
                }
                if (action.get() == ButtonType.OK) {
                    reiniciarJuego();
                } else {
                    System.exit(0);
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Error");
            alert.setContentText("Esa palabra no existe");
            alert.showAndWait();
            palabraEnJuego = "";
        }

        for (int i = 0; i < 27; i++) {
            if (key[i] == 1) {
                teclas[i].getStyleClass().clear();
                teclas[i].getStyleClass().add("Gris");
            }
            if (key[i] == 2) {
                teclas[i].getStyleClass().clear();
                teclas[i].getStyleClass().add("Amarillo");
            }

            if (key[i] == 3) {
                teclas[i].getStyleClass().clear();
                teclas[i].getStyleClass().add("Verde");
            }

            if(key[i]==0){
                    teclas[i].getStyleClass().clear();
                    teclas[i].getStyleClass().add("Inicio");
            }
        }

    }

    private void borrarLetra() {
        if (lJuego != 0) {
            lJuego--;
            letras[pJuego][lJuego].setText("-");
            letras[pJuego][lJuego].getStyleClass().clear();
            letras[pJuego][lJuego].getStyleClass().add("BordeClaro");
        }
    }

    private void ponerLetra(String letra) {

        if (lJuego < 5) {
            letras[pJuego][lJuego].setText(letra);
            letras[pJuego][lJuego].getStyleClass().clear();
            letras[pJuego][lJuego].getStyleClass().add("BordeOscuro");
            lJuego++;
        }

        if (lJuego > 5) {
            lJuego = 4;
        }
    }

    private void reiniciarJuego() {

        for (int i = 0; i < p; i++) {
            for (int j = 0; j < l; j++) {
                letras[i][j].setText(" - ");
                letras[i][j].getStyleClass().clear();
            }
        }
        
        
        pJuego = 0;
        lJuego = 0;
        palabraAJugar();
    }

    private void palabraAJugar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = (Connection) DriverManager.getConnection(cadConex + basededatos, usuario, passwd);
            System.out.println("Estamos conectados");
            //sabemos que hay 10835 usando count en la base de datos
            int pos = (new Random()).nextInt(10835);
            PreparedStatement contarPalabras = con.prepareStatement("SELECT * FROM palabras LIMIT " + pos + ",1");
            ResultSet rs = contarPalabras.executeQuery();
            rs.next();

            palabra = rs.getString("palabra");
            System.out.println(palabra);

            con.close();
            System.out.println("Estamos desconectados");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Wordle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Wordle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean existePalabra() {
        boolean salida = true;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = (Connection) DriverManager.getConnection(cadConex + basededatos, usuario, passwd);
            int pos = (new Random()).nextInt(10835);
            PreparedStatement contarPalabras = con.prepareStatement("SELECT count(*) as existe FROM " + "palabras WHERE palabra like '" + palabraEnJuego + "'");
            ResultSet rs = contarPalabras.executeQuery();
            rs.next();
            String existe = rs.getString("existe");

            if (existe.equals("0")) {
                salida = false;
            }
            con.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Wordle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Wordle.class.getName()).log(Level.SEVERE, null, ex);
        }

        return salida;
    }
}
