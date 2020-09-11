package dk.mwn.si.rmidb.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class BankImplementation extends UnicastRemoteObject implements BankInterface {
    // public static String url = "jdbc:h2:mem:Bank";
    public static String url = "jdbc:h2:file:./src/main/resources/db/bank";
    public static String user = "sa";
    public static String password = "";
    public static String driver = "org.h2.Driver";

    BankImplementation() throws RemoteException {
    }

    @GetMapping("/hello")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    //@GetMapping("/bank")
    public List<Customer> getMillionaires() {
        List<Customer> list = new ArrayList<Customer>();
        try {
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement ps = con.prepareStatement("select * from Customer where amount >= 100000;");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Customer c = new Customer();
                c.setAccnum(rs.getLong(1));
                c.setName(rs.getString(2));
                c.setAmount(rs.getDouble(3));
                System.out.println(c);
                list.add(c);
            }
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    private int addJsonCustomers(File jsonFile) {
        try {
            StringBuilder sb = new StringBuilder();
            FileReader fileReader = new FileReader(jsonFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String customer;
            while ((customer = bufferedReader.readLine()) != null)
                sb.append(customer);

            bufferedReader.close();
            fileReader.close();
                ObjectMapper mapper = new ObjectMapper();
                List<Customer> customers = Arrays.asList(mapper.readValue(sb.toString(), Customer[].class));

            return addListOfCustomers(customers);
        } catch (Exception e) {
            System.out.println("File could not be read: " + e.getMessage());
        }
        return 0;
    }

    private int addXmlCustomers(File xmlFile) {

        return 0;
    }

    private int addCSVCustomers(File csvFile) {
        List<Customer> customers = new ArrayList<>();
        Customer c = null;
        try (
                FileReader fileReader = new FileReader(csvFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                CSVReader csvReader = new CSVReader(bufferedReader);
        ) {
            List<String[]> records = csvReader.readAll();
            for (String[] record : records) {
                c = new Customer();
                c.setName(record[0]);
                c.setAmount(Double.valueOf(record[1]));
                customers.add(c);
            }
        } catch (Exception e) {
            System.out.println("Could not parse CSV: " + e.getMessage());
        }
        return addListOfCustomers(customers);
    }

    @Override
    public int addCustomers(File file) throws RemoteException, FileNotFoundException {
        switch (getFileExtension(file)) {
            case ".json":
                return addJsonCustomers(file);
            case ".csv":
                return addCSVCustomers(file);
            case ".xml":
                System.out.println("xml file");
                break;
            default:
                throw new FileNotFoundException("Not a recognizable fileformat");
        }
        return 0;
    }

    @Override
    public int getTotalCustomers() throws RemoteException {
        try {
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT COUNT(*) as ROW_COUNT FROM Customer");
            int result;
            if (rs.next())
                result = rs.getInt("ROW_COUNT");
            else
                result = 0;
            con.close();
            return result;
        } catch (Exception e) {
            System.out.println("BURN : " + e.getMessage());
        }
        return 0;
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }

    private int addListOfCustomers(List<Customer> customers) {
        try {
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);

            for (Customer customer : customers) {
                //PreparedStatement ps = con.prepareStatement("INSERT INTO Customer(accnum, name, amount) VALUES (?,?,?)");
                PreparedStatement ps = con.prepareStatement("INSERT INTO Customer(name, amount) VALUES (?,?)");

                //ps.setLong(1, customer.getAccnum());
                ps.setString(1, customer.getName());
                ps.setDouble(2, customer.getAmount());
                ps.executeUpdate();
            }
            con.close();

        } catch (Exception e) {
            System.out.println("Mapping failed: " + e.getMessage());
        }
        return customers.size();
    }

}