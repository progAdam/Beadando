package hu.projekt.rushhour.io;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.ExitPoint;
import hu.projekt.rushhour.model.Orientation;
import hu.projekt.rushhour.model.PlayerTurn;
import hu.projekt.rushhour.model.Position;
import hu.projekt.rushhour.model.SavedGame;
import hu.projekt.rushhour.model.Vehicle;
import hu.projekt.rushhour.model.VehicleRole;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class XmlStateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlStateManager.class);


    public void saveGameToXml(SavedGame savedGame, Path filePath) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("rushHourGame");
            root.setAttribute("playerName", savedGame.playerName());
            root.setAttribute("currentTurn", savedGame.currentTurn().name());
            root.setAttribute("turnCount", String.valueOf(savedGame.turnCount()));
            doc.appendChild(root);

            BoardState board = savedGame.boardState();
            Element boardEl = doc.createElement("board");
            boardEl.setAttribute("rows", String.valueOf(board.rows()));
            boardEl.setAttribute("cols", String.valueOf(board.cols()));
            root.appendChild(boardEl);

            boardEl.appendChild(createExitElement(doc, "humanExit", board.humanExit()));
            boardEl.appendChild(createExitElement(doc, "aiExit", board.aiExit()));

            Element vehiclesEl = doc.createElement("vehicles");
            boardEl.appendChild(vehiclesEl);
            for (Vehicle v : board.vehicles()) {
                Element vEl = doc.createElement("vehicle");
                vEl.setAttribute("id", v.id());
                vEl.setAttribute("row", String.valueOf(v.start().row()));
                vEl.setAttribute("col", String.valueOf(v.start().col()));
                vEl.setAttribute("length", String.valueOf(v.length()));
                vEl.setAttribute("orientation", v.orientation().name());
                vEl.setAttribute("role", v.role().name());
                vehiclesEl.appendChild(vEl);
            }

            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(doc), new StreamResult(filePath.toFile()));
            LOGGER.info("Saved active game XML to {}", filePath);
        } catch (Exception e) {
            throw new IOException("Failed to write XML game state: " + e.getMessage(), e);
        }
    }


    public SavedGame loadGameFromXml(Path filePath) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(filePath.toFile());
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            String playerName = root.getAttribute("playerName");
            PlayerTurn currentTurn = PlayerTurn.valueOf(root.getAttribute("currentTurn"));
            int turnCount = Integer.parseInt(root.getAttribute("turnCount"));

            Element boardEl = (Element) root.getElementsByTagName("board").item(0);
            int rows = Integer.parseInt(boardEl.getAttribute("rows"));
            int cols = Integer.parseInt(boardEl.getAttribute("cols"));

            ExitPoint humanExit = parseExitElement((Element) boardEl.getElementsByTagName("humanExit").item(0));
            ExitPoint aiExit = parseExitElement((Element) boardEl.getElementsByTagName("aiExit").item(0));

            NodeList vehicleNodes = boardEl.getElementsByTagName("vehicle");
            List<Vehicle> vehicles = new ArrayList<>();
            for (int i = 0; i < vehicleNodes.getLength(); i++) {
                Element vEl = (Element) vehicleNodes.item(i);
                vehicles.add(new Vehicle(
                        vEl.getAttribute("id"),
                        new Position(
                                Integer.parseInt(vEl.getAttribute("row")),
                                Integer.parseInt(vEl.getAttribute("col"))
                        ),
                        Integer.parseInt(vEl.getAttribute("length")),
                        Orientation.valueOf(vEl.getAttribute("orientation")),
                        VehicleRole.valueOf(vEl.getAttribute("role"))
                ));
            }

            BoardState boardState = new BoardState(rows, cols, vehicles, humanExit, aiExit);
            LOGGER.info("Loaded active game XML from {}", filePath);
            return new SavedGame(playerName, currentTurn, turnCount, boardState);
        } catch (Exception e) {
            throw new IOException("Failed to load XML game state: " + e.getMessage(), e);
        }
    }

    private Element createExitElement(Document doc, String tagName, ExitPoint exit) {
        Element el = doc.createElement(tagName);
        el.setAttribute("row", String.valueOf(exit.position().row()));
        el.setAttribute("col", String.valueOf(exit.position().col()));
        el.setAttribute("direction", exit.exitDirection().name());
        return el;
    }

    private ExitPoint parseExitElement(Element el) {
        return new ExitPoint(
                new Position(
                        Integer.parseInt(el.getAttribute("row")),
                        Integer.parseInt(el.getAttribute("col"))
                ),
                Direction.valueOf(el.getAttribute("direction"))
        );
    }
}
