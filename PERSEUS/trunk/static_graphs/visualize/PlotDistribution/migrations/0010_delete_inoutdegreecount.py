# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0009_auto_20141209_1735'),
    ]

    operations = [
        migrations.DeleteModel(
            name='InoutdegreeCount',
        ),
    ]
