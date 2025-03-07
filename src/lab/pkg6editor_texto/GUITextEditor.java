package lab.pkg6editor_texto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.undo.UndoManager;

public class GUITextEditor extends JFrame{

    
   private JTextPane areaTexto;
    private JComboBox<String> fuentes;
    private JComboBox<Integer> tamaños;
    private JButton btnColor, btnGuardar, btnAbrir;
    private JButton btnNegrita, btnCursiva, btnSubrayado;
    private JButton btnDeshacer, btnRehacer;
    private UndoManager gestorDeshacer;

    private JLabel lblRuta;
    private File directorioRaiz;

    private JPanel barraEstilos;  
    private JTree arbolArchivos;
    private DefaultMutableTreeNode nodoRaiz;

    public GUITextEditor() {
        gestorDeshacer = new UndoManager();
        setTitle("WORD ESDRAS, MARIO, WILLIAM");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String rutaProyecto = System.getProperty("user.dir");
        File carpetaProyecto = new File(rutaProyecto, "Documentos");
        if (!carpetaProyecto.exists()) {
            carpetaProyecto.mkdirs();
        }
        directorioRaiz = carpetaProyecto;

        areaTexto = new JTextPane();
        areaTexto.getDocument().addUndoableEditListener(e -> {
            gestorDeshacer.addEdit(e.getEdit());
            actualizarEstadoBotones();
        });
        JScrollPane scrollTexto = new JScrollPane(areaTexto);
        add(scrollTexto, BorderLayout.CENTER);

        JPanel barraSuperior = crearBarraSuperior();
        add(barraSuperior, BorderLayout.NORTH);

        barraEstilos = crearBarraEstilos();
        add(barraEstilos, BorderLayout.WEST);

        inicializarArbol(directorioRaiz);

        setVisible(true);
    }

    private JPanel crearBarraSuperior() {
        JPanel barraSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        barraSuperior.setBackground(Color.blue);

        btnColor = new JButton("Cambiar color");
        btnDeshacer = new JButton("Deshacer");
        btnRehacer = new JButton("Rehacer");

        fuentes = new JComboBox<>(GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames());

        tamaños = new JComboBox<>(new Integer[]{12, 14, 16, 18, 20, 24, 28, 32, 36, 48});

        fuentes.addActionListener(e -> aplicarFormato());
        tamaños.addActionListener(e -> aplicarFormato());

        btnDeshacer.addActionListener(e -> deshacer());
        btnRehacer.addActionListener(e -> rehacer());
        btnDeshacer.setEnabled(false);
        btnRehacer.setEnabled(false);

        btnNegrita = crearBotonEstilo("N", Font.BOLD, e -> alternarNegrita());
        btnCursiva = crearBotonEstilo("I", Font.ITALIC, e -> alternarCursiva());
        btnSubrayado = crearBotonEstilo("U", Font.PLAIN, e -> alternarSubrayado());
        btnColor.addActionListener(e -> seleccionarColor());

        barraSuperior.add(new JLabel("Fuente de la letra :"));
        barraSuperior.add(fuentes);
        barraSuperior.add(new JLabel("Tamaño de la letra :"));
        barraSuperior.add(tamaños);
        barraSuperior.add(btnNegrita);
        barraSuperior.add(btnCursiva);
        barraSuperior.add(btnSubrayado);
        barraSuperior.add(btnColor);
        barraSuperior.add(btnDeshacer);
        barraSuperior.add(btnRehacer);

        return barraSuperior;
    }

    private JPanel crearBarraEstilos() {
        JPanel barra = new JPanel();
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBorder(BorderFactory.createTitledBorder("GUARDAR Y ABRIR"));
        barra.setBackground(Color.blue);

        btnGuardar = new JButton("Guardar Archivo");
        btnAbrir = new JButton("Abrir Archivo");
        btnGuardar.addActionListener(e -> guardarArchivo());
        btnAbrir.addActionListener(e -> abrirArchivo());

        barra.add(btnGuardar);
        barra.add(Box.createVerticalStrut(5));
        barra.add(btnAbrir);
        barra.add(Box.createVerticalStrut(10));

        lblRuta = new JLabel("Carpeta raiz: (ninguna)");
        lblRuta.setForeground(Color.white);
        barra.add(lblRuta);
        barra.add(Box.createVerticalStrut(10));

        return barra;
        
    }
     private void inicializarArbol(File carpeta) {
        nodoRaiz = new DefaultMutableTreeNode(carpeta.getName());
        cargarNodos(carpeta, nodoRaiz);
        arbolArchivos = new JTree(nodoRaiz);

        JScrollPane scrollArbol = new JScrollPane(arbolArchivos);
        barraEstilos.add(scrollArbol);

        lblRuta.setText("Carpeta raiz: " + carpeta.getAbsoluteFile().getParentFile().getName());
        revalidate();
        repaint();
    }

    private void cargarNodos(File carpeta, DefaultMutableTreeNode nodoPadre) {
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(f.getName());
                nodoPadre.add(nodoHijo);
                if (f.isDirectory()) {
                    cargarNodos(f, nodoHijo);
                }
            }
        }
    }

    private JButton crearBotonEstilo(String texto, int estiloFuente, ActionListener listener) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", estiloFuente, 14));
        boton.addActionListener(listener);
        return boton;
    }

    private void alternarNegrita() {
        StyledDocument doc = areaTexto.getStyledDocument();
        int inicio = areaTexto.getSelectionStart();
        int fin = areaTexto.getSelectionEnd();
        if (inicio == fin) {
            JOptionPane.showMessageDialog(this, "Selecciona texto antes de aplicar formato.");
            return;
        }
        for (int i = inicio; i < fin; i++) {
            Element elemento = doc.getCharacterElement(i);
            AttributeSet attrs = elemento.getAttributes();
            SimpleAttributeSet nuevos = new SimpleAttributeSet(attrs);
            boolean esNegrita = StyleConstants.isBold(attrs);
            StyleConstants.setBold(nuevos, !esNegrita);
            doc.setCharacterAttributes(i, 1, nuevos, true);
        }
    }

    private void alternarCursiva() {
        StyledDocument doc = areaTexto.getStyledDocument();
        int inicio = areaTexto.getSelectionStart();
        int fin = areaTexto.getSelectionEnd();
        if (inicio == fin) {
            JOptionPane.showMessageDialog(this, "Selecciona texto antes de aplicar formato.");
            return;
        }
        for (int i = inicio; i < fin; i++) {
            Element elemento = doc.getCharacterElement(i);
            AttributeSet attrs = elemento.getAttributes();
            SimpleAttributeSet nuevos = new SimpleAttributeSet(attrs);
            boolean esCursiva = StyleConstants.isItalic(attrs);
            StyleConstants.setItalic(nuevos, !esCursiva);
            doc.setCharacterAttributes(i, 1, nuevos, true);
        }
    }

    private void alternarSubrayado() {
        StyledDocument doc = areaTexto.getStyledDocument();
        int inicio = areaTexto.getSelectionStart();
        int fin = areaTexto.getSelectionEnd();
        if (inicio == fin) {
            JOptionPane.showMessageDialog(this, "Selecciona texto antes de aplicar formato.");
            return;
        }
        for (int i = inicio; i < fin; i++) {
            Element elemento = doc.getCharacterElement(i);
            AttributeSet attrs = elemento.getAttributes();
            SimpleAttributeSet nuevos = new SimpleAttributeSet(attrs);
            boolean esSubrayado = StyleConstants.isUnderline(attrs);
            StyleConstants.setUnderline(nuevos, !esSubrayado);
            doc.setCharacterAttributes(i, 1, nuevos, true);

        }
    }
    
    private void deshacer() {
        if (gestorDeshacer.canUndo()) {
            gestorDeshacer.undo();
            actualizarEstadoBotones();
        }
    }

    private void rehacer() {
        if (gestorDeshacer.canRedo()) {
            gestorDeshacer.redo();
            actualizarEstadoBotones();
        }
    }

    private void actualizarEstadoBotones() {
        btnDeshacer.setEnabled(gestorDeshacer.canUndo());
        btnRehacer.setEnabled(gestorDeshacer.canRedo());
    }

    private void aplicarFormato() {
        StyledDocument doc = areaTexto.getStyledDocument();
        int inicio = areaTexto.getSelectionStart();
        int fin = areaTexto.getSelectionEnd();
        if (inicio == fin) {
            JOptionPane.showMessageDialog(this, "Selecciona texto antes de aplicar formato.");
            return;
        }
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        String fuente = (String) fuentes.getSelectedItem();
        if (fuente != null) {
            StyleConstants.setFontFamily(estilo, fuente);
        }
        Integer tamano = (Integer) tamaños.getSelectedItem();
        if (tamano != null) {
            StyleConstants.setFontSize(estilo, tamano);
        }
        doc.setCharacterAttributes(inicio, fin - inicio, estilo, false);
    }

    private void seleccionarColor() {
        StyledDocument doc = areaTexto.getStyledDocument();
        int inicio = areaTexto.getSelectionStart();
        int fin = areaTexto.getSelectionEnd();
        if (inicio == fin) {
            JOptionPane.showMessageDialog(this, "Selecciona texto antes de aplicar formato.");
            return;
        }
        Color color = JColorChooser.showDialog(this, "Seleccionar Color", Color.BLACK);
        if (color != null) {
            SimpleAttributeSet estilo = new SimpleAttributeSet();
            StyleConstants.setForeground(estilo, color);
            doc.setCharacterAttributes(inicio, fin - inicio, estilo, false);
        }
    }

    private void guardarArchivo() {
        JFileChooser selector = new JFileChooser(directorioRaiz);
        int opcion = selector.showSaveDialog(this);
        if (opcion == JFileChooser.APPROVE_OPTION) {
            File archivo = selector.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                RTFEditorKit editorKit = new RTFEditorKit();
                editorKit.write(fos, areaTexto.getStyledDocument(), 0, areaTexto.getStyledDocument().getLength());
                JOptionPane.showMessageDialog(this, "Archivo guardado con exito.");
                directorioRaiz = archivo.getParentFile();
                recargarArbol();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage());
            }
        }
    }

    private void abrirArchivo() {
        JFileChooser selector = new JFileChooser(directorioRaiz);
        int opcion = selector.showOpenDialog(this);
        if (opcion == JFileChooser.APPROVE_OPTION) {
            File archivo = selector.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(archivo)) {
                RTFEditorKit editorKit = new RTFEditorKit();
                StyledDocument doc = new DefaultStyledDocument();
                editorKit.read(fis, doc, 0);
                areaTexto.setStyledDocument(doc);
                JOptionPane.showMessageDialog(this, "Archivo abierto con exito.");
                gestorDeshacer.discardAllEdits();
                actualizarEstadoBotones();
                areaTexto.getDocument().addUndoableEditListener(e -> {
                    gestorDeshacer.addEdit(e.getEdit());
                    actualizarEstadoBotones();
                });
                directorioRaiz = archivo.getParentFile();
                recargarArbol();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir el archivo: " + ex.getMessage());
            }
        }
    }

    private void recargarArbol() {
        nodoRaiz.removeAllChildren();
        cargarNodos(directorioRaiz, nodoRaiz);
        // Se llama a getAbsoluteFile().getParentFile().getName() en lugar de getAbsolutePath()
        lblRuta.setText("Carpeta raiz: " + directorioRaiz.getAbsoluteFile().getParentFile().getName());
        ((DefaultTreeModel) arbolArchivos.getModel()).reload();
        revalidate();
        repaint();
    }
}
