package dk.mwn.si.rmidb.client;

import dk.mwn.si.rmidb.server.BankInterface;
import dk.mwn.si.rmidb.server.Customer;

import java.io.File;
import java.rmi.Naming;
import java.util.List;

public class RMIClient
{
    public static void main(String args[])throws Exception
    {
        // name =  rmi:// + ServerIP +  /EngineName;
        String remoteEngine = "rmi://localhost/BankServices";

        // Create local stub, lookup in the registry searching for the remote engine - the interface with the methods we want to use remotely
        BankInterface obj = (BankInterface) Naming.lookup(remoteEngine);

        obj.addCustomers(new File("src/main/resources/customers.json"));
        System.out.println(obj.getTotalCustomers());
    }

}
