package menu.controller;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import menu.domain.Category;
import menu.domain.CategoryRepository;
import menu.domain.Coach;
import menu.domain.CoachRepository;
import menu.domain.Menu;
import menu.domain.MenuRepository;
import menu.view.InputView;
import menu.view.OutputView;

public class MainController {
    private final InputView inputView;
    private final OutputView outputView;
    private final Map<ApplicationStatus, Supplier<ApplicationStatus>> gameGuide;

    public MainController(InputView inputView, OutputView outputView) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.gameGuide = new EnumMap<>(ApplicationStatus.class); // 밑에 status 있음
        initializeGameGuide();
    }

    private void initializeGameGuide() {
        gameGuide.put(ApplicationStatus.INITIALIZE_MENUS, this::initializeMenus);
        gameGuide.put(ApplicationStatus.RECEIVE_COACH_DATA, this::receiveCoachData);
        gameGuide.put(ApplicationStatus.RECOMMEND_RANDOM_MENU, this::recommendRandomMenu);
    }

    public void play() {
        ApplicationStatus applicationStatus = process(ApplicationStatus.INITIALIZE_MENUS); // 초기 status
        while (applicationStatus.playable()) {
            applicationStatus = process(applicationStatus);
        }
    }

    public ApplicationStatus process(ApplicationStatus applicationStatus) {
        try {
            return gameGuide.get(applicationStatus).get();
        } catch (NullPointerException exception) {
            return ApplicationStatus.APPLICATION_EXIT;
        }
    }

    private ApplicationStatus initializeMenus() {
        new InitializingController().process();
        return ApplicationStatus.RECEIVE_COACH_DATA;
    }

    private ApplicationStatus receiveCoachData() {
        outputView.printStart();
        inputView.readCoachNames().forEach(CoachRepository::add);
        System.out.println(MenuRepository.menus());
        for (Coach coach : CoachRepository.coaches()) {
            List<Menu> menuNotToEat = inputView.readMenuNotToEat(coach.getName());
            coach.addMenuNotToEat(menuNotToEat);
        }
        return ApplicationStatus.RECOMMEND_RANDOM_MENU;
    }

    private ApplicationStatus recommendRandomMenu() {
        for (Coach coach : CoachRepository.coaches()) {
            Category category = CategoryRepository.pickRandomCategory();
            Menu menu = category.pickRandomMenu();
            System.out.println(menu);
        }

        return ApplicationStatus.APPLICATION_EXIT;
    }

    private enum ApplicationStatus {
        INITIALIZE_MENUS,
        RECEIVE_COACH_DATA,
        RECOMMEND_RANDOM_MENU,

        APPLICATION_EXIT;

        public boolean playable() {
            return this != APPLICATION_EXIT;
        }
    }

}