package ar.edu.itba;

/**
 * Hello world!
 *
 */
public class App {
    Embed embed;
    Extract extract;

    public void main(String[] args) {
        String action = args[0];
        if (action.equals("embed"))
            embed.embed(args);
        else if (action.equals("extract"))
            extract.extract(args);
    }

}
