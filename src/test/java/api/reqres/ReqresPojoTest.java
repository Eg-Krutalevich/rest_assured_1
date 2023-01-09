package api.reqres;

import api.reqres.pojo.Register;
import api.reqres.pojo.SuccessReg;
import api.reqres.pojo.UnSuccessReg;
import api.reqres.pojo.ColorData;
import api.reqres.spec.Specifications;
import api.reqres.pojo.UserData;
import api.reqres.pojo.UserTime;
import api.reqres.pojo.UserTimeResponse;
import org.junit.Assert;
import org.junit.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class ReqresPojoTest {
    private final static String URL = "https://reqres.in";

    @Test
    public void checkAvatarAndIdTest() {
        //GET
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(200));
        List<UserData> users = given() //
                .get("/api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);

        //1 СПОСОБ
        //сравнение, что в avatar содержится id, x - счетчик экземплера класса
        users.forEach(x -> Assert.assertTrue(x.getAvatar().contains(x.getId().toString())));

        //сравнение, что email оканчивается на @reqres.in
        Assert.assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith("@reqres.in")));

        //2 СПОСОБ
        //сравнение, что в avatar содержится id
        List<String> avatars = users.stream().map(UserData::getAvatar).toList(); //преоразовать к List (toList())
        List<String> ids = users.stream().map(x -> x.getId().toString()).toList(); //преобразовали к String

        for (int i = 0; i < avatars.size(); i++) {
            Assert.assertTrue(avatars.get(i).contains(ids.get(i)));
        }

    }

    //POST
    //создание успешного пользователя
    @Test
    public void successRegTest() {
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(200));

        //ожидаемый результат
        Integer id = 4;
        String token = "QpwL5tke4Pnpja7X4";

        //пользователь
        Register user = new Register("eve.holt@reqres.in", "pistol");
        SuccessReg successReg = given()
                .body(user)
                .when() //куда отправляется запрос
                .post("/api/register")
                .then().log().all()
                .extract().as(SuccessReg.class);

        Assert.assertNotNull(successReg.getId());
        Assert.assertNotNull(successReg.getToken());

        Assert.assertEquals(id, successReg.getId());
        Assert.assertEquals(token, successReg.getToken());

    }

    //создание пользователя с ошибкой 400
    @Test
    public void unSuccessRegTest() {
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(400));

        String error = "Missing password";
        //пользователь
        Register user = new Register("sydney@fife", "");
        UnSuccessReg unSuccessReg = given()
                .body(user)
                .when() //куда отправляется запрос
                .post("/api/register")
                .then().log().all()
                .extract().as(UnSuccessReg.class);

        Assert.assertEquals(error, unSuccessReg.getError());
        Assert.assertNotNull(unSuccessReg.getError());
    }

    @Test
    public void sortYearsTest() {
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(200));

        List<ColorData> colors = given() //
                .get("/api/unknown")
                .then().log().all()
                .extract().body().jsonPath().getList("data", ColorData.class);

        //actual result
        List<Integer> years = colors.stream().map(ColorData::getYear).collect(Collectors.toList());
        //excepted result
        List<Integer> sortYears = years.stream().sorted().collect(Collectors.toList());

        Assert.assertEquals(sortYears, years);
        System.out.println(years);
        System.out.println(sortYears);

    }

    //DELETE
    @Test
    public void deleteUserTest() {
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(204));
        given()
                .when()
                .delete("/api/users/2")
                .then().log().all();

    }

    //сравнение времена сервера и времени из документации
    @Test
    public void timeTest() {
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(200));

        UserTime user = new UserTime("morpheus", "zion resident");

        UserTimeResponse response = given()
                .body(user)
                .when()
                .put("/api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);

        String regex = "(.{5})$";
        String currentTime = Clock.systemUTC().instant().toString().replaceAll(regex, "");

        Assert.assertEquals(currentTime, response.getUpdatedAt().replaceAll(regex, ""));

        System.out.println(currentTime);
        System.out.println(response.getUpdatedAt().replaceAll(regex, ""));

    }

}
