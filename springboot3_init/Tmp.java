import com.sakura.boot_init.support.auth.TokenManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.mockito.Mockito.*;
public class Tmp {
  public static void main(String[] args) {
    TokenManager tm = new TokenManager(mock(StringRedisTemplate.class));
    for(int i=0;i<5;i++) {
      String t = tm.generateToken();
      System.out.println(t);
      System.out.println(t.matches("^[0-9A-Za-z!@#$%^&*()_+\\-=\\[\\]{};:,.?/]+$"));
      System.out.println(t.matches(".*[0-9].*"));
      System.out.println(t.matches(".*[A-Za-z].*"));
      System.out.println(t.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:,.?/].*"));
    }
  }
}
