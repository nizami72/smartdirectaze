package az.nizami.smartdirectaze.shared;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Util {

    private static long adminUserId;

    public Util(@Value("${telegram.bot.admin.id}") long adminUserId) {
        Util.adminUserId = adminUserId;
    }

    public static boolean checkIfAdmin(Long useId){
        return useId == adminUserId;
    }

}
