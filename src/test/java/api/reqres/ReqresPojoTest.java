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
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(200));
        List<UserData> users = given() //
                .get("/api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);

        users.forEach(x -> Assert.assertTrue(x.getAvatar().contains(x.getId().toString())));
        
        Assert.assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith("@reqres.in")));

        List<String> avatars = users.stream().map(UserData::getAvatar).toList(); //преоразовать к List (toList())
        List<String> ids = users.stream().map(x -> x.getId().toString()).toList(); //преобразовали к String

        for (int i = 0; i < avatars.size(); i++) {
            Assert.assertTrue(avatars.get(i).contains(ids.get(i)));
        }

    }

    @Test
    public void successRegTest() {
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(200));
        
        Integer id = 4;
        String token = "QpwL5tke4Pnpja7X4";

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

    public void unSuccessRegTest() {
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(400));

        String error = "Missing password";
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

        List<Integer> years = colors.stream().map(ColorData::getYear).collect(Collectors.toList());
        List<Integer> sortYears = years.stream().sorted().collect(Collectors.toList());

        Assert.assertEquals(sortYears, years);
        System.out.println(years);
        System.out.println(sortYears);

    }
    
    @Test
    public void deleteUserTest() {
        Specifications.installSpecification(Specifications.requestSpecification(URL), Specifications.responseSpecificationUnknown(204));
        given()
                .when()
                .delete("/api/users/2")
                .then().log().all();

    }

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
