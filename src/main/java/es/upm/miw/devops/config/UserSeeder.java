package es.upm.miw.devops.config;

import es.upm.miw.devops.model.Role;
import es.upm.miw.devops.model.User;
import es.upm.miw.devops.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    public UserSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("Iniciando el seeder: Generando 50 usuarios...");

            List<User> usersToSave = new ArrayList<>();
            Random random = new Random();

            String[] firstNames = { "Alejandro", "María", "David", "Carmen", "Daniel", "Ana", "Javier", "Laura", "José",
                    "Isabel", "Elena", "Carlos", "Marta", "Manuel", "Lucía" };
            String[] familyNames = { "García", "Fernández", "González", "Rodríguez", "López", "Martínez", "Sánchez",
                    "Pérez", "Gómez", "Martín", "Ruiz", "Díaz" };
            String[][] locations = {
                    { "Madrid", "Madrid", "28001" }, { "Barcelona", "Barcelona", "08001" },
                    { "Valencia", "Valencia", "46001" }, { "Sevilla", "Sevilla", "41001" },
                    { "Zaragoza", "Zaragoza", "50001" }, { "Málaga", "Málaga", "29001" },
                    { "Alicante", "Alicante", "03001" }, { "Bilbao", "Vizcaya", "48001" }
            };
            String[] streets = { "Calle Mayor", "Gran Vía", "Calle Alcalá", "Paseo de la Castellana",
                    "Avenida Diagonal", "Calle Atocha", "Calle Princesa" };
            String dniLetters = "TRWAGMYFPDXBNJZSQVHLCKE";

            for (int i = 1; i <= 50; i++) {
                String fname = firstNames[random.nextInt(firstNames.length)];
                String lname = familyNames[random.nextInt(familyNames.length)];

                int dniNum = 10000000 + random.nextInt(90000000);
                String identity = dniNum + String.valueOf(dniLetters.charAt(dniNum % 23));

                String email = fname.toLowerCase() + "." + lname.toLowerCase() + i + "@ejemplo.com";
                String address = streets[random.nextInt(streets.length)] + " " + (random.nextInt(150) + 1);
                String[] loc = locations[random.nextInt(locations.length)];

                boolean active = random.nextDouble() > 0.1;

                // Asignación usando el enumerador Role
                Role userRole;
                if (i <= 3) {
                    userRole = Role.ADMIN;
                } else if (i % 2 == 0) {
                    userRole = Role.VENDOR;
                } else {
                    userRole = Role.OPERATOR;
                }

                User user = new User();
                user.setFirstName(fname);
                user.setFamilyName(lname);
                user.setIdentity(identity);
                user.setEmail(email);
                user.setAddress(address);
                user.setCity(loc[0]);
                user.setProvince(loc[1]);
                user.setPostalCode(loc[2]);
                user.setActive(active);
                user.setRole(userRole);

                usersToSave.add(user);
            }

            userRepository.saveAll(usersToSave);
            System.out.println("Seeder completado: 50 usuarios creados.");
        } else {
            System.out.println("La tabla 'users' ya tiene datos. Seeder omitido.");
        }
    }
}