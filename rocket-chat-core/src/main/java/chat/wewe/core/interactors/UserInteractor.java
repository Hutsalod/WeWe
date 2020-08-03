package chat.wewe.core.interactors;

import java.util.List;

import chat.wewe.core.models.User;
import chat.wewe.core.repositories.UserRepository;
import io.reactivex.Flowable;

public class UserInteractor {

  private final UserRepository userRepository;

  public UserInteractor(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Flowable<List<User>> getUserAutocompleteSuggestions(String name) {
    return userRepository.getSortedLikeName(name, 5);
  }
}
