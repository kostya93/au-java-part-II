import Util.Repository;
import Util.RepositoryImpl;

/**
 * Created by kostya on 25.09.2016.
 */
public class MyGit {
    public static void main(String[] args) {
        Repository repository = new RepositoryImpl();
        repository.execCommand(args).forEach(System.out::println);
    }
}
