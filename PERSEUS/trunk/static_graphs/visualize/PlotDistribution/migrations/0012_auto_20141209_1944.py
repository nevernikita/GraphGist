# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0011_inoutdegreecount_node_radiuscount'),
    ]

    operations = [
        migrations.DeleteModel(
            name='Node',
        ),
        migrations.DeleteModel(
            name='RadiusCount',
        ),
    ]
