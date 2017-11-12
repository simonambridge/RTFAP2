dsetool create_core rtfap.transactions generateResources=true reindex=true
dsetool reload_core rtfap.transactions schema=./transactions.xml reindex=true

dsetool create_core rtfap.txn_count_min generateResources=true reindex=true
dsetool reload_core rtfap.txn_count_min schema=./txn_count_min.xml reindex=true
