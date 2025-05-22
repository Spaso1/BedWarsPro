package org.ast.bedwarspro.mannger;

import org.ast.bedwarspro.been.Plot;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlotManager {
    private final List<Plot> plots = new ArrayList<>();
    private final File dataFile;

    public PlotManager(File dataFolder) {
        this.dataFile = new File(dataFolder, "plots.dat");
        loadPlots();
    }

    public void addPlot(Plot plot) {
        plots.add(plot);
        savePlots();
    }

    public Plot getPlotAtLocation(org.bukkit.Location loc) {
        for (Plot plot : plots) {
            if (plot.isInside(loc)) {
                return plot;
            }
        }
        return null;
    }

    public List<Plot> getPlayerPlots(String playerName) {
        List<Plot> playerPlots = new ArrayList<>();
        for (Plot plot : plots) {
            if (plot.getOwner().equals(playerName)) {
                playerPlots.add(plot);
            }
        }
        return playerPlots;
    }

    public void savePlots() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(plots);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlots() {
        if (!dataFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            List<Plot> loadedPlots = (List<Plot>) ois.readObject();
            plots.addAll(loadedPlots);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}