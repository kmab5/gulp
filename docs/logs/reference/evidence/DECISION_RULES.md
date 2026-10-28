# Hydration engine and language policy

## Structured model of truth
`intake_event` = user-reported beverage and its recorded volume, timestamp, and time-zone metadata. It does not measure net fluid absorption or blood/urine osmolality. `daily_logged_ml` = sum of nondeleted beverage events assigned to a local calendar day. It is **not** full total-water intake and not a hydration score.

## Rules (requirements)
- H-01 Adults only in first release; onboarding asks only necessary routine/preferences. Optional safety gate asks whether clinician advised restriction/specific plan. If yes, suspend all automatically derived volume targets and redirect to self-entry or reminders-only.
- H-02 Offer zero numerical goal, user's own volume reference, or clearly labeled population-level **beverage tracking illustration**. No automatic quota by weight.
- H-03 Accept water/tea/coffee/milk/other with precise ml; use equal volume accounting for ordinary drinks without arbitrary correction. Be clear about sugar/caffeine where discussed—nutritional equivalence is not implied.
- H-04 Reject negative/zero/implausible accidental values via validation and confirmation (not a clinical upper limit). Editable correction/undo always available.
- H-05 Quiet hours block nudges; user-selected interval determines candidate events, not compulsory drink times.
- H-06 On a drink logged within a configured suppression window before the next reminder, suppress that candidate once. A log must not cause continuous notifications.
- H-07 Dismiss means no immediate repeated nagging. 'Not thirsty' is a valid successful user choice, not failure.
- H-08 Missed notifications create no water debt or backlog requirement. Do not send catch-up notifications on app relaunch.
- H-09 Optional target reached: no volume-seeking reminders; user can still log and voluntarily keep routine reminders with explicit opt-in.
- H-10 Local historical missing days are 'no entries', never 'no water consumed'.
- H-11 Road visual reward follows meaningful use, exploration and returns, never drinking extra volume; caps protect against volume-gaming.
- H-12 Reflection uses 'recorded', 'logged' and 'may' when warranted; never 'you were dehydrated', 'hydration 82%', 'you prevented kidney stones'.
- H-13 Specialized situations and illness: offer calm educational prompts or disable generic targets, do not algorithmically diagnose or prescribe.
- H-14 Deletion/export and notifications control must be available offline.

## Decision matrix
| Condition | Show | Never show |
| --- | --- | --- |
| 1,200 mL logged today | '1,200 mL logged' | '60% hydrated' |
| No entries yesterday | 'No drinks recorded yesterday' | 'You didn't drink yesterday' |
| Several missed reminders | 'Your next gentle reminder is...' | 'You owe 900 mL' |
| Declines because not thirsty | 'Got it. We'll leave you be.' | 'Your body needs this now' |
| Reference achieved | 'You've reached your chosen logging reference' | 'You are definitely hydrated' |
| Hot/active context user-selected | 'Needs may be higher; respond to thirst and conditions' | 'Drink exactly 650 mL more' |

## Research governance
Every feature that implies physiological benefit receives: source DOI/URL; population; endpoint; grade A/B/C/P; exclusion criteria; exact approved copy; updated-at timestamp. Don't deploy new health wording by changing design strings alone. Clinical review needed if expanding to pregnant/breastfeeding clinical targets, kidney disease, endurance-athlete prescriptions, elderly medical guidance or pediatric users.
