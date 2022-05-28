import sqlite3
import random


conn = sqlite3.connect('app/src/main/assets/populated_db.db')

words = 'Nietzsche defined master morality as the morality of the strong-willed. He criticizes the view (which he identifies with contemporary British ideology) that good is everything that is helpful, and bad is everything that is harmful. He argues proponents of this view have forgotten its origins and that it is based merely on habit: what is useful has always been defined as good, therefore usefulness is goodness as a value. He writes that in the prehistoric state "the value or non-value of an action was derived from its consequences"[1] but that ultimately "[t]here are no moral phenomena at all, only moral interpretations of phenomena."[2] For strong-willed men, the "good" is the noble, strong, and powerful, while the "bad" is the weak, cowardly, timid, and petty. '.split(" ")

rows = []
for c in 'Nietzsche defined master morality as the morality of the strong-willed. He criticizes the view (which he identifies with contemporary British ideology) that good is everything that is helpful, and bad is everything that is harmful. He argues proponents of this view have forgotten its origins and that it is based merely on habit: what is useful has always been defined as good, therefore usefulness is goodness as a value. He writes that in the prehistoric state "the value or non-value of an action was derived from its consequences"[1] but that ultimately "[t]here are no moral phenomena at all, only moral interpretations of phenomena."[2] For strong-willed men, the "good" is the noble, strong, and powerful, while the "bad" is the weak, cowardly, timid, and petty.':
    rows.append((None, random.choice(words), c))

c = conn.cursor()
c.executemany('insert into Quote values (?,?,?)', rows)
conn.commit()
conn.close()