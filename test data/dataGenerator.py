import pandas as pd
from datetime import date

#range
start = date(2025, 8, 11)
end   = date(2026, 6, 15)


all_days = pd.date_range(start=start, end=end, freq='D')

# A–H for weekdays weekends = "0"
letters = ['A','B','C','D','E','F','G','H']
cycle_letters = []
weekday_counter = 0

for d in all_days:
    if d.weekday() < 5:  # Mon-Fri
        cycle_letters.append(letters[weekday_counter % len(letters)])
        weekday_counter += 1
    else:  # Sat/Sun
        cycle_letters.append("0")

df = pd.DataFrame({
    'date': all_days.date.astype(str),
    'weekday': all_days.day_name(),
    'cycle_letter': cycle_letters
})

# Save CSV
path = "letter_day_calendar.csv"
df.to_csv(path, index=False)

