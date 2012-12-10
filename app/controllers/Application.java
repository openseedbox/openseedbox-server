package controllers;

import play.jobs.OnApplicationStart;
import play.mvc.Controller;

public class Application extends Controller {

    public static void index() {
        render();
    }

}