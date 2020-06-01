
/*
*   OVERVIEW:
*   Stampa delle info commands
 */

public class Help {
    public static void help(){
        System.out.println(
                "usage : turing COMMAND [ ARGS ...]\n" +
                "\n" +
                "commands :\n" +
                " register < username > < password >  registra l' utente\n" +
                " login < username > < password >     effettua il login\n" +
                " logout                              effettua il logout\n" +
                "\n" +
                " create < doc > < numsezioni >       crea un documento\n" +
                " share < doc > < username >          condivide il documento\n" +
                " show < doc > < sec >                mostra una sezione del documento\n" +
                " show < doc >                        mostra l' intero documento\n" +
                " list                                mostra la lista dei documenti\n" +
                "\n" +
                " edit < doc > < sec >                modifica una sezione del documento\n" +
                "       send < msg >                        invia un msg sulla chat\n" +
                "       receive                             visualizza i msg ricevuti sulla chat\n"+
                " end-edit < doc > < sec >            fine modifica della sezione del doc\n" +
                "N.B. -Quando crei un file, per garantire l'univocita' del nome, verra' riconosciuto come:\n" +
                "      [nome file]_[tuo nome]\n\n"+
                "     -Le sezioni del file partono da zero! Quindi se vuoi editare la prima sezione sara':\n" +
                "      turing edit [nome file]_[mio nome] 0\n"
                );
    }
}
