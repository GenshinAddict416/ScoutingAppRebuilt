import pandas as pd
import tkinter as tk
from tkinter import ttk
import matplotlib.pyplot as plt

# 1. Load Data
df = pd.read_csv('Scouting_Merged.csv')

# --- Helper: Convert '16-30' ranges into the midpoint number ---
# --- Updated Helper: Handles 'None' by returning 0.0 ---
def convert_amount(val):
    # Convert everything to string to handle potential NaN/Nulls first
    val_str = str(val).strip().lower()
    
    if val_str in ['none', 'nan', '']:
        return 0.0  # Treats "None" as a zero for the average
    
    if '-' in val_str:
        try:
            low, high = val_str.split('-')
            return (float(low) + float(high)) / 2
        except:
            return 0.0
            
    return pd.to_numeric(val, errors='coerce') if val_str != 'none' else 0.0

# Apply to columns
df['AutoAmount_Num'] = df['AutoAmount'].apply(convert_amount).fillna(0.0)
df['TeleopAmount_Num'] = df['TeleopAmount'].apply(convert_amount).fillna(0.0)

# Re-calculate averages with the fixed numbers
team_stats = df.groupby('Team').mean(numeric_only=True)
teams = sorted(df['Team'].unique())

class FRCApp:
    def __init__(self, root):
        self.root = root
        self.root.title("FRC Team Profiler")
        self.root.geometry("900x600")

        # --- LEFT SIDE: Search + Scrollable Team List ---
        self.left_container = tk.Frame(root, width=250)
        self.left_container.pack(side="left", fill="y", padx=10, pady=10)

        tk.Label(self.left_container, text="Search Team:", font=('Arial', 10, 'bold')).pack()
        self.search_var = tk.StringVar()
        self.search_var.trace_add("write", self.filter_teams)
        self.search_entry = tk.Entry(self.left_container, textvariable=self.search_var)
        self.search_entry.pack(fill="x", pady=5)

        self.canvas = tk.Canvas(self.left_container, width=200)
        self.scrollbar = ttk.Scrollbar(self.left_container, orient="vertical", command=self.canvas.yview)
        self.scroll_frame = tk.Frame(self.canvas)

        self.scroll_frame.bind("<Configure>", lambda e: self.canvas.configure(scrollregion=self.canvas.bbox("all")))
        self.canvas.create_window((0, 0), window=self.scroll_frame, anchor="nw")
        self.canvas.configure(yscrollcommand=self.scrollbar.set)

        self.canvas.pack(side="left", fill="both", expand=True)
        self.scrollbar.pack(side="right", fill="y")

        # --- RIGHT SIDE: Info Display ---
        self.right_frame = tk.Frame(root)
        self.right_frame.pack(side="right", expand=True, fill="both", padx=10)
        
        self.title_label = tk.Label(self.right_frame, text="Select a team", font=('Arial', 16, 'bold'))
        self.title_label.pack(pady=10)
        
        self.stats_text = tk.Text(self.right_frame, height=15, width=60, font=('Courier', 10))
        self.stats_text.pack(pady=5)
        
        self.graph_btn = tk.Button(self.right_frame, text="📈 Show Accuracy Graph", state="disabled")
        self.graph_btn.pack(pady=10, ipadx=10)

        self.team_buttons = {}
        self.create_buttons()

    def create_buttons(self):
        for team in teams:
            t_int = int(team)
            btn = tk.Button(self.scroll_frame, text=f"Team {t_int}", 
                            command=lambda t=team: self.show_profile(t))
            btn.pack(fill="x", pady=1)
            self.team_buttons[t_int] = btn

    def filter_teams(self, *args):
        search_term = self.search_var.get()
        for team_num, btn in self.team_buttons.items():
            if search_term == "" or search_term in str(team_num):
                btn.pack(fill="x", pady=1)
            else:
                btn.pack_forget()

    # MOVED: This is now correctly aligned with the other functions
    def show_profile(self, team_num):
        self.title_label.config(text=f"Team {int(team_num)} Profile")
        self.stats_text.delete('1.0', tk.END)
        
        # 1. Get Averages
        stats = team_stats.loc[team_num]
        
        # 2. Get Match History (with fixed NaN display)
        matches = df[df['Team'] == team_num][['Match', 'AutoAmount', 'TeleopAmount']].fillna("None")
        
        # 3. NEW: Extract and clean comments for this team
        # dropna() removes empty rows; str(c).strip() != "" skips blank spaces
        raw_comments = df[df['Team'] == team_num]['Comments'].dropna()
        team_notes = [str(c) for c in raw_comments if str(c).strip() != "" and str(c).lower() != 'nan']

        # --- BUILD THE DISPLAY TEXT ---
        summary = "--- PERFORMANCE AVERAGES ---\n"
        display_map = {
            "Auto Accuracy": "AutoAccuracy",
            "Auto Amount": "AutoAmount_Num",
            "Teleop Accuracy": "TeleopAccuracy",
            "Teleop Amount": "TeleopAmount_Num"
        }

        for label, col in display_map.items():
            val = stats.get(col, 0.0)
            summary += f"{label}: {0.00 if pd.isna(val) else val:.2f}\n"

        summary += "\n--- MATCH HISTORY ---\n"
        summary += matches.to_string(index=False)
        
        # NEW: Add the Comments section if any exist
        if team_notes:
            summary += "\n\n--- SCOUT COMMENTS ---\n"
            summary += "\n".join([f"• {note}" for note in team_notes])
        else:
            summary += "\n\n--- NO COMMENTS RECORDED ---"
        
        self.stats_text.insert(tk.END, summary)
        self.graph_btn.config(state="normal", command=lambda: self.plot_team(team_num), bg="#4CAF50", fg="white")

    def plot_team(self, team_num):
        team_data = df[df['Team'] == team_num].sort_values('Match')
        plt.figure(f"Team {int(team_num)} Analysis")
        plt.plot(team_data['Match'], team_data['AutoAccuracy'], marker='o', label='Auto Acc')
        plt.plot(team_data['Match'], team_data['TeleopAccuracy'], marker='s', label='Teleop Acc')
        plt.title(f"Accuracy Trend: Team {int(team_num)}")
        plt.xlabel("Match Number")
        plt.ylabel("Accuracy %")
        plt.legend()
        plt.grid(True, linestyle='--', alpha=0.7)
        plt.show()

if __name__ == "__main__":
    root = tk.Tk()
    app = FRCApp(root)
    root.mainloop()
