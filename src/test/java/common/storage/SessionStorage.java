package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();

    private SessionStorage() {}

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user: users){
            INSTANCE.get().userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
    }

    /**
     * Возвращаем объект CreateUserRequest по его порядковому номеру в списке созданных пользователей.
     * @param number Порядковый номер, начиная с 1 (а не с 0)
     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру.
     */

    public static CreateUserRequest getUser(int number){
      return new ArrayList<>(INSTANCE.get().userStepsMap.keySet()).get(number-1);
    }

    public static CreateUserRequest getUser(){
        return getUser(1);
    }

    public static UserSteps getSteps(){
        return getSteps(1);
    }

    public static UserSteps getSteps(int number){
        return new ArrayList<>(INSTANCE.get().userStepsMap.values()).get(number-1);
    }

    public static void clear(){
        INSTANCE.get().userStepsMap.clear();
    }
}
