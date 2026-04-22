package com.liquidify;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.liquidify.gui.LiquidifyLauncher;
import javax.swing.SwingUtilities;
import java.io.File;
import java.util.List;

public class Injector {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LiquidifyLauncher::new);
    }

    public static boolean performInjection() {
        try {
            String pid = null;
            List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
            
            for (VirtualMachineDescriptor vmd : vmds) {
                String name = vmd.displayName().toLowerCase();
                if (name.contains("net.minecraft.client.main.main") || 
                    name.contains("minecraft") || 
                    name.contains("javaw")) {
                    pid = vmd.id();
                    break;
                }
            }

            if (pid == null) return false;

            File agentFile = new File("liquidify.jar");
            if (!agentFile.exists()) return false;

            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(agentFile.getAbsolutePath());
            vm.detach();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
