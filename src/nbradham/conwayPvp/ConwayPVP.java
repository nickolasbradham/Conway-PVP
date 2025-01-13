package nbradham.conwayPvp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

final class ConwayPVP {

	private static final short INIT_DELAY = 200;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Conway PVP");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			JPanel controls = new JPanel();
			ConwayPanel conwayPane = new ConwayPanel(50, 50);
			conwayPane.setBorder(new LineBorder(Color.BLACK));
			JButton step = new JButton("Step"), playPause = new JButton("Play/Pause"), clear = new JButton("Clear");
			step.addActionListener(e -> conwayPane.step());
			controls.add(step);
			Timer time = new Timer(INIT_DELAY, e -> conwayPane.step());
			playPause.addActionListener(e -> {
				if (time.isRunning())
					time.stop();
				else
					time.start();
			});
			controls.add(playPause);
			controls.add(new JLabel("Delay (ms):"));
			JSpinner delay = new JSpinner(new SpinnerNumberModel(INIT_DELAY, 1, Integer.MAX_VALUE, 25));
			delay.setPreferredSize(new Dimension(50, delay.getPreferredSize().height));
			delay.addChangeListener(e -> time.setDelay((int) delay.getValue()));
			controls.add(delay);
			clear.addActionListener(e -> conwayPane.clear());
			controls.add(clear);
			frame.add(controls, BorderLayout.NORTH);
			frame.add(conwayPane, BorderLayout.CENTER);
			frame.pack();
			frame.setResizable(false);
			frame.setVisible(true);
		});
	}

	private static final class ConwayPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private static final byte CELL_W = 16;

		private boolean[][] grid;
		private HashSet<Point> qHash = new HashSet<>();
		private Queue<Point> que = new LinkedList<>();

		private ConwayPanel(int gridW, int gridH) {
			super();
			setPreferredSize(
					new Dimension((grid = new boolean[gridW][gridH]).length * CELL_W, grid[0].length * CELL_W));
			MouseAdapter adapter = new MouseAdapter() {

				private static final Point P_NULL = new Point(-1, -1);

				private Point last = P_NULL;

				@Override
				public void mousePressed(MouseEvent e) {
					handle(e);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					last = P_NULL;
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					int x = e.getX(), y = e.getY();
					if (x > -1 && x < getWidth() && y > -1 && y < getHeight())
						handle(e);
				}

				private void handle(MouseEvent e) {
					int x = e.getX() / CELL_W, y = e.getY() / CELL_W;
					if (x != last.x || y != last.y) {
						grid[x][y] = !grid[x][y];
						offerNeighbors(x, y, qHash, que);
						offer(last = new Point(x, y), qHash, que);
						repaint();
					}
				}
			};
			addMouseListener(adapter);
			addMouseMotionListener(adapter);
		}

		@Override
		public void paint(Graphics g) {
			for (byte x = 0; x < grid.length; ++x)
				for (byte y = 0; y < grid[x].length; ++y) {
					g.setColor(grid[x][y] ? Color.BLACK : Color.WHITE);
					g.fillRect(x * CELL_W, y * CELL_W, CELL_W, CELL_W);
				}
			super.paintBorder(g);
		}

		private void step() {
			Queue<Point> newQ = new LinkedList<>(), toToggle = new LinkedList<>();
			HashSet<Point> newQHash = new HashSet<>();
			while (!que.isEmpty()) {
				Point p = que.poll();
				byte[] count = { 0 };
				forNeighbors(p.x, p.y, n -> {
					if (grid[n.x][n.y])
						++count[0];
				});
				if ((grid[p.x][p.y] && (count[0] < 2 || count[0] > 3)) || (!grid[p.x][p.y] && count[0] == 3)) {
					toToggle.offer(p);
					offerNeighbors(p.x, p.y, newQHash, newQ);
				}
			}
			while (!toToggle.isEmpty()) {
				Point p = toToggle.poll();
				grid[p.x][p.y] = !grid[p.x][p.y];
			}
			que = newQ;
			qHash = newQHash;
			repaint();
		}

		private void offerNeighbors(int x, int y, HashSet<Point> qh, Queue<Point> q) {
			forNeighbors(x, y, p -> offer(p, qh, q));
		}

		private void forNeighbors(int x, int y, Consumer<Point> c) {
			for (byte dx = -1; dx != 2; ++dx)
				for (byte dy = -1; dy != 2; ++dy)
					if (dx != 0 || dy != 0)
						c.accept(new Point(Math.floorMod(x + dx, grid.length), Math.floorMod(y + dy, grid[0].length)));
		}

		private void clear() {
			grid = new boolean[grid.length][grid[0].length];
			qHash.clear();
			que.clear();
		}

		private static void offer(Point p, HashSet<Point> qh, Queue<Point> q) {
			if (qh.add(p))
				q.offer(p);
		}
	}
}