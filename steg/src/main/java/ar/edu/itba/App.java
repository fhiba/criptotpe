package ar.edu.itba;

/**
 * Hello world!
 *
 */
public class App {

    public void main(String[] args) {
        String action = args[0];
        if (action.equals("embed"))
            Embed.embed(args);
        else if (action.equals("extract"))
            Extract.extract(args);
    }

}
